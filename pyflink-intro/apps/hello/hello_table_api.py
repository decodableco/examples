import logging
import sys

from pyflink.table import (
    EnvironmentSettings, TableEnvironment, DataTypes, TableDescriptor, Schema)
from pyflink.table.expressions import col,lit


def hello_pyflink():

    # environment
    t_env = TableEnvironment.create(EnvironmentSettings.in_streaming_mode())
    
    # source table (basic in-mem data generator)
    t_env.create_table("generator_source", 
                       TableDescriptor.for_connector("datagen")
                       .schema(Schema.new_builder()
                               .column("num", DataTypes.BIGINT())
                               .build())
                       .option("rows-per-second", "1")
                       .build())
    
    # sink table (just printing to stdout)
    t_env.create_table("print_sink", 
                       TableDescriptor.for_connector("print")
                       .schema(Schema.new_builder()
                               .column("num", DataTypes.BIGINT())
                               .column("hello", DataTypes.STRING())
                               .build())
                       .build())
    
    # read source -> process -> write sink
    t_env.from_path("generator_source").select(
        col("num").abs % 10,lit('hello 🐍 pyflink 🐿️ ')
    ).execute_insert("print_sink").wait()

if __name__ == '__main__':
    logging.basicConfig(stream=sys.stdout,
                        level=logging.INFO, format="%(message)s")
    hello_pyflink()
