CREATE TABLE `addresses` (
  `id` int NOT NULL,
  `customer_id` int NOT NULL,
  `street` varchar(255) NOT NULL,
  `city` varchar(255) NOT NULL,
  `state` varchar(255) NOT NULL,
  `zip` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`) NOT ENFORCED) WITH (
                                          'connector' = 'mysql-cdc',
                                          'hostname' = 'mysql',
                                          'port' = '3306',
                                          'username' = 'debezium',
                                          'password' = 'dbz',
                                          'database-name' = 'inventory',
                                          'table-name' = 'addresses');

CREATE TABLE `customers` (
  `id` int NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  PRIMARY KEY (`id`) NOT ENFORCED)
  WITH (
     'connector' = 'mysql-cdc',
     'hostname' = 'mysql',
     'port' = '3306',
     'username' = 'debezium',
     'password' = 'dbz',
     'database-name' = 'inventory',
     'table-name' = 'customers');

--CREATE TABLE `geom` (
--  `id` int NOT NULL,
--  `g` geometry NOT NULL,
--  `h` geometry DEFAULT NULL,
--  PRIMARY KEY (`id`) NOT ENFORCED) WITH (
--                                         'connector' = 'mysql-cdc',
--                                         'hostname' = 'mysql',
--                                         'port' = '3306',
--                                         'username' = 'debezium',
--                                         'password' = 'dbz',
--                                         'database-name' = 'inventory',
--                                         'table-name' = 'geom');
--[ERROR] Could not execute SQL statement. Reason:
--org.apache.calcite.sql.validate.SqlValidatorException: Geo-spatial extensions and the GEOMETRY data type are not enabled


CREATE TABLE `orders` (
  `order_number` int NOT NULL,
  `order_date` date NOT NULL,
  `purchaser` int NOT NULL,
  `quantity` int NOT NULL,
  `product_id` int NOT NULL,
  PRIMARY KEY (`order_number`) NOT ENFORCED) WITH (
                                                   'connector' = 'mysql-cdc',
                                                   'hostname' = 'mysql',
                                                   'port' = '3306',
                                                   'username' = 'debezium',
                                                   'password' = 'dbz',
                                                   'database-name' = 'inventory',
                                                   'table-name' = 'orders');

CREATE TABLE `products` (
  `id` int NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(512),
  `weight` float ,
  PRIMARY KEY (`id`) NOT ENFORCED) WITH (
                                         'connector' = 'mysql-cdc',
                                         'hostname' = 'mysql',
                                         'port' = '3306',
                                         'username' = 'debezium',
                                         'password' = 'dbz',
                                         'database-name' = 'inventory',
                                         'table-name' = 'products');
                                      
CREATE TABLE `products_on_hand` (
  `product_id` int NOT NULL,
  `quantity` int NOT NULL,
  PRIMARY KEY (`product_id`) NOT ENFORCED) WITH (
                                                 'connector' = 'mysql-cdc',
                                                 'hostname' = 'mysql',
                                                 'port' = '3306',
                                                 'username' = 'debezium',
                                                 'password' = 'dbz',
                                                 'database-name' = 'inventory',
                                                 'table-name' = 'products_on_hand');
