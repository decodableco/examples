import logging
import requests
import json

import os
os.environ['HF_HOME'] = '/opt/flink-libs/hf-hub-cache'

import sys

from pyflink.table import (EnvironmentSettings, TableEnvironment, DataTypes)
from pyflink.table.udf import ScalarFunction, udf, FunctionContext
from sentence_transformers import SentenceTransformer

class RemoteEmbedding(ScalarFunction):
  
  def __init__(self, url):
      self.modelServerApiUrl = url
  
  def eval(self, text):
    response = requests.post(self.modelServerApiUrl,
                             data=text.encode('utf-8'),
                             headers={'Content-type': 'text/plain; charset=utf-8'})
    return json.loads(response.text)
  
class LocalEmbedding(ScalarFunction):
  
  model: None
  
  def __init__(self):
    self.model = SentenceTransformer(model_name_or_path='all-mpnet-base-v2',cache_folder='/opt/flink-libs/hf-hub-cache')

  def open(self, function_context: FunctionContext):
    self.model = SentenceTransformer(model_name_or_path='all-mpnet-base-v2',cache_folder='/opt/flink-libs/hf-hub-cache')
 
  def eval(self, text):
    return self.model.encode(text,None,None,32,None,'sentence_embedding','float32',True,False).tolist()
  
  def __getstate__(self):
    state = dict(self.__dict__)
    del state['model']
    return state
  
def processReviews():

    # single secret containing the full job config as JSON object
    # with open('./.secrets/job_config', 'r') as file:
    #     job_config = json.loads(file.read())

    with open('/opt/pipeline-secrets/vector_ingest_job_config', 'r') as file:
        job_config = json.loads(file.read())

    t_env = TableEnvironment.create(EnvironmentSettings.in_streaming_mode())
    t_env.get_config().set("parallelism.default", "1")

    t_env.create_temporary_system_function(
       "remote_embedding",
       udf(
          RemoteEmbedding(job_config['model_server']),
          input_types=[DataTypes.STRING()],
          result_type=DataTypes.ARRAY(DataTypes.DOUBLE())
        )
    )

    t_env.create_temporary_system_function(
       "local_embedding",
       udf(
          LocalEmbedding(),
          input_types=[DataTypes.STRING()],
          result_type=DataTypes.ARRAY(DataTypes.DOUBLE())
        )
    )

    fs_base_path_libs = os.path.abspath(os.path.dirname(__file__)) + "/libs"
    
    mysql_driver_jar = os.path.join(fs_base_path_libs,
                            'mysql-connector-java-8.0.30.jar')
    
    flink_mysqlcdc_jar = os.path.join(fs_base_path_libs,
                            'flink-sql-connector-mysql-cdc-3.1.0.jar')
    
    flink_sql_mongodb_jar = os.path.join(fs_base_path_libs,
                            'flink-sql-connector-mongodb-1.2.0-1.19.jar')
    
    flink_python_jar = os.path.join(fs_base_path_libs,
                            'flink-python-1.19.1.jar')

    t_env.get_config() \
            .get_configuration() \
            .set_string("pipeline.jars", 
                        "file://{}".format(mysql_driver_jar)
                        + ";file://{}".format(flink_mysqlcdc_jar)
                        + ";file://{}".format(flink_sql_mongodb_jar)
                        + ";file://{}".format(flink_python_jar)
            )

    # define table source based on mysql-cdc connector
    t_env.execute_sql(f"""
                CREATE TABLE reviews (
                    id BIGINT,
                    itemId STRING,
                    reviewText STRING,
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
                    'table-name' = 'Review');
                """)
    
    # define table sink based on mongodb connector
    t_env.execute_sql(f"""
                CREATE TABLE review_embeddings (
                    _id BIGINT,
                    itemId STRING,
                    reviewText STRING,
                    embedding DOUBLE ARRAY,
                    PRIMARY KEY (_id) NOT ENFORCED
                ) WITH (
                    'connector' = 'mongodb',
                    'uri' = '{job_config['mongodb_uri']}',
                    'database' = 'vector_demo',
                    'collection' = 'review_embeddings'
                )
            """)

    # switch between local or remote embedding calculation according to configuration
    calc_embedding = "";
    if job_config['embedding_mode'] == 'remote':
        calc_embedding = 'REMOTE_EMBEDDING(reviewText) AS embedding'
    elif job_config['embedding_mode'] == 'local':
        calc_embedding = 'LOCAL_EMBEDDING(reviewText) AS embedding'
    else:
       print(f"error: embedding_mode must be either [local | remote] but was '{job_config['embedding_mode']}'")
       sys.exit(-1)
   
    # read from source table, calculate vector embeddings according to chosen mode, and insert into table sink
    t_env.execute_sql(
            f"""
                INSERT INTO review_embeddings
                    SELECT
                        id AS _id,
                        itemId,
                        reviewText,
                        {calc_embedding}
                    FROM reviews
            """
    )#.wait()

if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format="%(message)s")
    processReviews()
