CREATE TABLE `es_customers` (
  `id` int NOT NULL,
  `first_name` varchar(255) NOT NULL,
  `last_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  PRIMARY KEY (`id`) NOT ENFORCED)  WITH ('connector'='elasticsearch-7', 'hosts'='http://elasticsearch:9200','index'='customers01');
INSERT INTO es_customers SELECT * FROM customers ;

-- All the followin need primary keys adding to them if you want updates/deletes to work
--CREATE TABLE `es_addresses` WITH ('connector'='elasticsearch-7', 'hosts'='http://elasticsearch:9200','index'='addresses')  AS SELECT * FROM addresses ;
--CREATE TABLE `es_customers` WITH ('connector'='elasticsearch-7', 'hosts'='http://elasticsearch:9200','index'='customers')  AS SELECT * FROM customers ;
--CREATE TABLE `es_orders` WITH ('connector'='elasticsearch-7', 'hosts'='http://elasticsearch:9200','index'='orders')  AS SELECT * FROM orders ;
--CREATE TABLE `es_products_on_hand` WITH ('connector'='elasticsearch-7', 'hosts'='http://elasticsearch:9200','index'='products_on_hand')  AS SELECT * FROM products_on_hand ;
--CREATE TABLE `es_products` WITH ('connector'='elasticsearch-7', 'hosts'='http://elasticsearch:9200','index'='products')  AS SELECT * FROM products ;
