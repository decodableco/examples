import logging
import sys

from pyflink.table import (EnvironmentSettings, TableEnvironment)


def hello_pyflink():

    # environment
    t_env = TableEnvironment.create(EnvironmentSettings.in_streaming_mode())
    
    # source table (in-mem data generator)
    t_env.execute_sql("""
   CREATE TABLE generator_source (
     num BIGINT
   ) WITH (
     'connector' = 'datagen',
     'rows-per-second' = '1'
   )""")

    # sink table (printer to stdout)
    t_env.execute_sql("""
   CREATE TABLE print_sink (
     num BIGINT,
     hello STRING
   ) WITH (
     'connector' = 'print'
   )""")

    # read source -> process -> write sink
    # using INSERT INTO ... SELECT ... FROM
    t_env.execute_sql("""
       INSERT INTO print_sink SELECT ABS(num) % 10 AS num, 'hello 🐍 pyflink 🐿️ ' AS hello FROM generator_source"""
                      ).wait()


if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout,
                        level=logging.INFO, format="%(message)s")
    hello_pyflink()
