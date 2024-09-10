SET 'table.exec.mini-batch.enabled' = 'true';
SET 'table.exec.mini-batch.allow-latency' = '500 ms';
SET 'table.exec.mini-batch.size' = '1000';

CREATE FUNCTION ARRAY_AGGR AS 'co.decodable.demos.arrayagg.ArrayAggr';

CREATE TABLE purchase_orders (
   id INT,
   order_date DATE,
   purchaser_id INT,
   PRIMARY KEY (id) NOT ENFORCED
)
WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'inventory',
   'table-name' = 'purchase_orders',
   'slot.name' = 'purchase_orders_slot'
);

CREATE TABLE order_lines (
   id INT,
   order_id INT,
   product_id INT,
   quantity INT,
   price DOUBLE,
   PRIMARY KEY (id) NOT ENFORCED
)
WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'inventory',
   'table-name' = 'order_lines',
   'slot.name' = 'order_lines_slot'
);

CREATE TABLE products (
   id INT,
   name VARCHAR(255),
   description VARCHAR(512),
   weight DOUBLE,
   PRIMARY KEY (id) NOT ENFORCED
)
WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'inventory',
   'table-name' = 'products',
   'slot.name' = 'products_slot'
);

CREATE TABLE customers (
   id INT,
   first_name STRING,
   last_name STRING,
   email STRING,
   PRIMARY KEY (id) NOT ENFORCED
)
WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'inventory',
   'table-name' = 'customers',
   'slot.name' = 'customers_slot'
);

CREATE TABLE customer_phone_numbers (
   id INT,
   customer_id INT,
   type VARCHAR(20),
   `value` VARCHAR(100),
   PRIMARY KEY (id) NOT ENFORCED
)
WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'inventory',
   'table-name' = 'customer_phone_numbers',
   'slot.name' = 'customer_phone_numbers_slot'
);

CREATE TABLE orders_with_lines_kafka (
  order_id INT,
  order_date DATE,
  purchaser_id INT,
  lines ARRAY<ROW<id INT, product_id INT, quantity INT, price DOUBLE>>,
  PRIMARY KEY (order_id) NOT ENFORCED
)
WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'orders_with_lines',
    'properties.bootstrap.servers' = 'redpanda:29092',
    'key.format' = 'json', 'value.format' = 'json'
);

CREATE TABLE orders_with_lines_and_customer_kafka (
  order_id INT,
  order_date DATE,
  purchaser ROW<id INT, first_name VARCHAR, last_name VARCHAR, email VARCHAR, phone_numbers ARRAY<ROW<id INT, type VARCHAR, `value` VARCHAR>>>,
  lines ARRAY<ROW<id INT, product_id INT, quantity INT, price DOUBLE>>,
  PRIMARY KEY (order_id) NOT ENFORCED
)
WITH (
    'connector' = 'upsert-kafka',
    'topic' = 'orders_with_lines_and_customer',
    'properties.bootstrap.servers' = 'redpanda:29092',
    'key.format' = 'json', 'value.format' = 'json'
);

CREATE TABLE orders_with_lines_es (
  order_id INT,
  order_date DATE,
  purchaser_id INT,
  lines ARRAY<ROW<id INT, product_id INT, quantity INT, price DOUBLE>>,
  PRIMARY KEY (order_id) NOT ENFORCED
)
WITH (
     'connector' = 'elasticsearch-7',
     'hosts' = 'http://elasticsearch:9200',
     'index' = 'orders_with_lines'
);

CREATE TABLE orders_with_lines_and_customer_es (
  order_id INT,
  order_date DATE,
  purchaser ROW<id INT, first_name VARCHAR, last_name VARCHAR, email VARCHAR, phone_numbers ARRAY<ROW<id INT, type VARCHAR, `value` VARCHAR>>>,
  lines ARRAY<ROW<id INT, product_id INT, quantity INT, price DOUBLE>>,
  PRIMARY KEY (order_id) NOT ENFORCED
)
WITH (
     'connector' = 'elasticsearch-7',
     'hosts' = 'http://elasticsearch:9200',
     'index' = 'orders_with_lines_and_customer'
);
