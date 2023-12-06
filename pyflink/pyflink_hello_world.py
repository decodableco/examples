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

from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import StreamTableEnvironment

def pyflink_hello_world():
    env = StreamExecutionEnvironment.get_execution_environment()
    env.set_parallelism(1)

    t_env = StreamTableEnvironment.create(stream_execution_environment=env)

    kafka_jar = os.path.join(os.path.abspath(os.path.dirname(__file__)),
                            'flink-sql-connector-kafka-3.0.2-1.18.jar')

    t_env.get_config()\
            .get_configuration()\
            .set_string("pipeline.jars", "file://{}".format(kafka_jar))

    t_env.execute_sql("""
    CREATE TABLE orders (
      order_number BIGINT,
      price        DECIMAL(32,2),
      buyer        ROW<first_name STRING, last_name STRING>,
      order_time   TIMESTAMP(3)
    ) WITH (
      'connector' = 'datagen',
      'rows-per-second' = '4'
    )""")

    t_env.execute_sql("""
    CREATE TABLE orders_sink (
      order_number BIGINT,
      price        DECIMAL(32,2),
      buyer        ROW<first_name STRING, last_name STRING>,
      order_time   TIMESTAMP(3)
    ) WITH (
      'connector' = 'kafka',
      'topic' = 'orders',
      'properties.bootstrap.servers' = 'my-cluster-kafka-bootstrap.kafka.svc.cluster.local:9092',
      'properties.group.id' = 'orders-sink',
      'format' = 'json'
    )""")

    t_env.execute_sql("""
        INSERT INTO orders_sink SELECT * FROM orders""")


if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout, level=logging.INFO, format="%(message)s")
    pyflink_hello_world()
