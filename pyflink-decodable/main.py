################################################################################
#  Licensed to the Apache Software Foundation (ASF) under one
#  or more contributor license agreements.  See the NOTICE file
#  distributed with this work for additional information
#  regarding copyright ownership.  The ASF licenses this file
#  to you under the Apache License, Version 2.0 (the
#  "License"); you may not use this file except in compliance
#  with the License.  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
# limitations under the License.
################################################################################
import logging
import sys
import os

import requests
import jmespath
import json
import sys

from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment, DataTypes
from pyflink.table.udf import udf

@udf(input_types=[DataTypes.BIGINT()], result_type=DataTypes.STRING())
def get_user_name(id):
    r = requests.get('https://jsonplaceholder.typicode.com/users/' + str(id))
    return jmespath.search("name", json.loads(r.text))

def process_todos():
    with open('/opt/pipeline-secrets/todo_kafka_user_name', 'r') as file:
      user_name = file.read()
    with open('/opt/pipeline-secrets/todo_kafka_password', 'r') as file:
      password = file.read()
    with open('/opt/pipeline-secrets/todo_kafka_bootstrap_servers', 'r') as file:
      bootstrap_servers = file.read()

    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_parallelism(1)

    t_env = StreamTableEnvironment.create(stream_execution_environment=env)
    t_env.create_temporary_system_function("user_name", get_user_name)

    kafka_jar = os.path.join(os.path.abspath(os.path.dirname(__file__)) + "/libs",
                            'flink-sql-connector-kafka-3.0.2-1.18.jar')

    flink_python_jar = os.path.join(os.path.abspath(os.path.dirname(__file__)) + "/libs",
                            'flink-python-1.18.1.jar')

    t_env.get_config()\
            .get_configuration()\
            .set_string("pipeline.jars", "file://{}".format(kafka_jar) + ";file://{}".format(flink_python_jar))

    t_env.execute_sql("""
    CREATE TABLE todos (
      id      BIGINT,
      text    STRING,
      user_id BIGINT,
      due     TIMESTAMP(3)
    ) WITH (
      'connector' = 'datagen',
      'rows-per-second' = '1'
    )""")

    t_env.execute_sql(f"""
    CREATE TABLE enriched_todos (
      id        BIGINT,
      text      STRING,
      user_id   BIGINT,
      due       TIMESTAMP(3),
      user_name STRING
    ) WITH (
      'connector' = 'kafka',
      'topic' = 'todos',
      'properties.bootstrap.servers' = '{bootstrap_servers}',
      'properties.sasl.mechanism' = 'SCRAM-SHA-256',
      'properties.security.protocol' = 'SASL_SSL',
      'properties.sasl.jaas.config' = 'org.apache.flink.kafka.shaded.org.apache.kafka.common.security.scram.ScramLoginModule required username=\"{user_name}\" password=\"{password}\";',
      'properties.group.id' = 'todos-sink',
      'format' = 'json'
    )""")

    t_env.execute_sql("""
        INSERT INTO enriched_todos SELECT *, user_name(ABS(MOD(todos.user_id, 10))) FROM todos""")

if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format="%(message)s")
    process_todos()
