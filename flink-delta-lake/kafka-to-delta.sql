CREATE TABLE t_k_orders
  (
     orderId          STRING,
     customerId       STRING,
     orderNumber      INT,
     product          STRING,
     backordered      BOOLEAN,
     cost             FLOAT,
     description      STRING,
     create_ts        BIGINT,
     creditCardNumber STRING,
     discountPercent  INT
  ) WITH (
    'connector'                    = 'kafka',
    'topic'                        = 'orders',
    'properties.bootstrap.servers' = 'broker:29092',
    'scan.startup.mode'            = 'earliest-offset',
    'format'                       = 'json'
  );

CREATE CATALOG c_delta WITH ( 'type' = 'delta-catalog', 'catalog-type' = 'in-memory');
CREATE DATABASE c_delta.db_1;

SET 'execution.checkpointing.interval' = '60sec';

CREATE TABLE c_delta.db_1.t_d_orders 
    WITH ('connector'  = 'delta',
          'table-path' = 's3a://warehouse/orders')
    AS
    SELECT * 
      FROM t_k_orders
     WHERE cost < 100;
