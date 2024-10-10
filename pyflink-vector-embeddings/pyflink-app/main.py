import logging
import requests
import json
import os
import sys

from pyflink.table import (EnvironmentSettings, TableEnvironment, DataTypes)
from pyflink.table.udf import ScalarFunction, udf

class Embedding(ScalarFunction):
  
  def __init__(self, url):
      self.modelServerApiUrl = url
  
  def eval(self, text):
    response = requests.post(self.modelServerApiUrl,
                             data=text.encode('utf-8'),
                             headers={'Content-type': 'text/plain; charset=utf-8'}
    )
    return json.loads(response.text)
  
def processReviews():

    with open('/opt/pipeline-secrets/vector_ingest_mysql_host', 'r') as file:
        mysql_host = file.read()
    with open('/opt/pipeline-secrets/vector_ingest_mysql_port', 'r') as file:
        mysql_port = file.read()
    with open('/opt/pipeline-secrets/vector_ingest_mysql_user', 'r') as file:
        mysql_user = file.read()
    with open('/opt/pipeline-secrets/vector_ingest_mysql_password', 'r') as file:
        mysql_password = file.read()
    with open('/opt/pipeline-secrets/vector_ingest_mongodb_uri', 'r') as file:
        mongodb_uri = file.read()
    with open('/opt/pipeline-secrets/vector_ingest_model_server', 'r') as file:
        model_server = file.read()

    t_env = TableEnvironment.create(EnvironmentSettings.in_streaming_mode())
    t_env.get_config().set("parallelism.default", "1")

    t_env.create_temporary_system_function(
       "embedding",
       udf(
          Embedding(model_server),
          input_types=[DataTypes.STRING()],
          result_type=DataTypes.ARRAY(DataTypes.DOUBLE())
        )
    )

    mysql_driver_jar = os.path.join(os.path.abspath(os.path.dirname(__file__)) + "/libs",
                            'mysql-connector-java-8.0.30.jar')
    
    flink_mysqlcdc_jar = os.path.join(os.path.abspath(os.path.dirname(__file__)) + "/libs",
                            'flink-sql-connector-mysql-cdc-3.1.0.jar')
    
    flink_sql_mongodb_jar = os.path.join(os.path.abspath(os.path.dirname(__file__)) + "/libs",
                            'flink-sql-connector-mongodb-1.2.0-1.18.jar')
    
    flink_python_jar = os.path.join(os.path.abspath(os.path.dirname(__file__)) + "/libs",
                            'flink-python-1.18.1.jar')

    t_env.get_config()\
            .get_configuration() \
            .set_string("pipeline.jars", "file://{}".format(mysql_driver_jar) + ";file://{}".format(flink_mysqlcdc_jar) + ";file://{}".format(flink_sql_mongodb_jar) + ";file://{}".format(flink_python_jar))

    # define table source based on mysql-cdc connector
    t_env.execute_sql(f"""
                CREATE TABLE reviews (
                    id BIGINT,
                    itemId STRING,
                    reviewText STRING,
                    PRIMARY KEY (id) NOT ENFORCED
                )  WITH (
                    'connector' = 'mysql-cdc',
                    'hostname' = '{mysql_host}',
                    'port' = '{mysql_port}',
                    'username' = '{mysql_user}',
                    'password' = '{mysql_password}',
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
                    'uri' = '{mongodb_uri}',
                    'database' = 'vector_demo',
                    'collection' = 'review_embeddings'
                )
            """)

    # read from source table, calculate vector embeddings, and insert into table sink
    t_env.execute_sql("""
            INSERT INTO review_embeddings
                SELECT
                      id AS _id,
                      itemId,
                      reviewText,
                      EMBEDDING(reviewText) AS embedding
                FROM reviews
        """)

if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format="%(message)s")
    processReviews()
