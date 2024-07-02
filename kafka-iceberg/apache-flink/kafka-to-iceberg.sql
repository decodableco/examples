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
    'connector' = 'kafka',
    'topic' = 'orders',
    'properties.bootstrap.servers' = 'broker:29092',
    'scan.startup.mode' = 'earliest-offset',
    'format' = 'json'
  );


SET 'execution.checkpointing.interval' = '60sec';
SET 'pipeline.operator-chaining.enabled' = 'false';

CREATE TABLE t_i_orders 
  WITH (
  'connector' = 'iceberg',
  'catalog-type'='hive',
  'catalog-name'='dev',
  'warehouse' = 's3a://warehouse',
  'hive-conf-dir' = './conf')
  AS 
  SELECT * FROM t_k_orders 
   WHERE cost > 100;