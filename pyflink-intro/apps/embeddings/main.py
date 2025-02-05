import json
import logging
import re
import sys
import os
os.environ['HF_HOME'] = os.path.abspath(os.path.dirname(__file__)) + '/.hf-cache'

from langchain_community.document_loaders import GutenbergLoader
from langchain_text_splitters import RecursiveCharacterTextSplitter, TextSplitter

from pyflink.table import (EnvironmentSettings, TableEnvironment, DataTypes)
from pyflink.table.udf import (ScalarFunction, udf, FunctionContext)

from sentence_transformers import SentenceTransformer

class LoadFromUrl(ScalarFunction):
    
    def eval(self,url):
        book = GutenbergLoader(url).load()
        text = re.sub(r'\n{2,}', '\n', book[0].page_content)
        return text

class SplitText(ScalarFunction):
    
    splitter: TextSplitter = None

    def __init__(self):
        self.splitter = self.getSplitter()

    def open(self, function_context: FunctionContext):
        self.splitter = self.getSplitter()

    def getSplitter(self):
        return RecursiveCharacterTextSplitter(
           chunk_size=2048,chunk_overlap=256,length_function=len,
           is_separator_regex=False,keep_separator=False
        )

    def eval(self, text):
        return self.splitter.split_text(text)

    def __getstate__(self):
        state = dict(self.__dict__)
        del state['splitter']
        return state

class CalcEmbedding(ScalarFunction):
  
  model: SentenceTransformer = None
  
  def __init__(self):
    self.model = self.getModel()

  def open(self, function_context: FunctionContext):
    self.model = self.getModel()
 
  def getModel(self):
      return SentenceTransformer(model_name_or_path='BAAI/bge-small-en-v1.5',cache_folder=os.environ['HF_HOME'])

  def eval(self, text):
    return self.model.encode(text).tolist()
  
  def __getstate__(self):
    state = dict(self.__dict__)
    del state['model']
    return state

def process_books(path_to_job_config, path_to_job_libs):

    # prepare table environment
    t_env = TableEnvironment.create(EnvironmentSettings.in_streaming_mode())

    # include source and sink connector related JARs from libs dir
    t_env.get_config() \
            .set("pipeline.jars", 
                        "file://{}".format(os.path.join(path_to_job_libs,'mysql-connector-java-8.0.30.jar'))
                        + ";file://{}".format(os.path.join(path_to_job_libs,'flink-sql-connector-mysql-cdc-3.1.0.jar'))
                        + ";file://{}".format(os.path.join(path_to_job_libs,'flink-sql-connector-mongodb-1.2.0-1.19.jar'))
                        + ";file://{}".format(os.path.join(path_to_job_libs,'flink-python-1.19.1.jar'))
            )
    
    # register 3 UDFs in table environment
    t_env.create_temporary_system_function(
        "load_from_url",udf(LoadFromUrl(),input_types=[DataTypes.STRING()],result_type=DataTypes.STRING())
    )

    t_env.create_temporary_system_function(
        "split_text",udf(SplitText(),input_types=[DataTypes.STRING()],result_type=DataTypes.ARRAY(DataTypes.STRING()))
    )

    t_env.create_temporary_system_function(
        "calc_embedding",udf(CalcEmbedding(),input_types=[DataTypes.STRING()],result_type=DataTypes.ARRAY(DataTypes.DOUBLE()))
    )

    # load configuration / credentials from file containing the full job config as single JSON object
    with open(path_to_job_config, 'r') as file:
        job_config = json.loads(file.read())

    # define source table based on mysql-cdc connector
    t_env.execute_sql(f"""
                CREATE TABLE book (
                    id BIGINT,
                    title STRING,
                    link STRING,
                    PRIMARY KEY (id) NOT ENFORCED
                )  WITH (
                    'connector' = 'mysql-cdc',
                    'hostname' = '{job_config['mysql_host']}',
                    'port' = '{job_config['mysql_port']}',
                    'username' = '{job_config['mysql_user']}',
                    'password' = '{job_config['mysql_password']}',
                    'jdbc.properties.maxAllowedPacket' = '16777216',
                    'server-time-zone' = 'UTC',
                    'database-name' = 'vector_demo',
                    'table-name' = 'book');
                """)
    
    # define sink table based on mongodb connector
    t_env.execute_sql(f"""
                CREATE TABLE book_embeddings (
                    _id STRING,
                    title STRING,
                    item STRING,
                    source STRING,
                    chunk STRING,
                    embedding DOUBLE ARRAY,
                    ts TIMESTAMP(3),
                    PRIMARY KEY (_id) NOT ENFORCED
                ) WITH (
                    'connector' = 'mongodb',
                    'uri' = '{job_config['mongodb_uri']}',
                    'database' = 'vector_demo',
                    'collection' = 'book_embeddings'
                )
            """)
    
    # data processing logic using the custom UDFs to: 
    # - load book content from URL
    # - split text into chunks
    # - calculate vector embeddings using sentence transformer with specific embedding model
    # - write results into the sink table which stores embeddings into MongoDB collection
    t_env.execute_sql(
        """ 
        INSERT INTO book_embeddings 
            SELECT UUID() AS _id,title,item,source,
                    chunk,CALC_EMBEDDING(chunk) AS embedding,CURRENT_ROW_TIMESTAMP() AS ts FROM ( 
                SELECT
                    title,
                    SHA256(link) as item,
                    link as source,
                    SPLIT_TEXT(LOAD_FROM_URL(link)) as segments FROM book
                ) CROSS JOIN UNNEST (segments) AS chunk;              
        """) #\
    #.wait()

if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout,level=logging.INFO, format="%(message)s")
    
    job_config_path = os.path.abspath(os.path.dirname(__file__)) + '/.secrets/job_config'
    if len(sys.argv) > 1:
        job_config_path = sys.argv[1]
    
    job_libs_path = os.path.abspath(os.path.dirname(__file__)) + "/libs"
    if len(sys.argv) > 2:
        job_libs_path = sys.argv[2] 

    process_books(job_config_path, job_libs_path)
