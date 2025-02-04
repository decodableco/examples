import logging
import random
import sys
import time

from pyflink.common.types import Row, RowKind
from pyflink.datastream import StreamExecutionEnvironment
from pyflink.table import (StreamTableEnvironment, DataTypes, TableDescriptor, Schema)

def hello_pyflink():

    # environment for both APIs
    s_env = StreamExecutionEnvironment.get_execution_environment()
    t_env = StreamTableEnvironment.create(s_env)
    
    # source table (in-mem data generator)
    # NOTE: no direct datastream equivalent for datagen in pyflink
    t_env.create_table("generator_source", 
                       TableDescriptor.for_connector("datagen")
                       .schema(Schema.new_builder()
                               .column("num", DataTypes.BIGINT())
                               .build())
                       .option("rows-per-second", "1")
                       .build())
    
    # process stream
    # here: map transformation then just print
    t_env.to_data_stream(t_env.from_path("generator_source")) \
        .map(lambda r: Row(abs(r.num) % 10,'hello 🐍 pyflink 🐿️ ')) \
        .print()
    
    s_env.execute().wait()

if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout,
                        level=logging.INFO, format="%(message)s")
    hello_pyflink()
