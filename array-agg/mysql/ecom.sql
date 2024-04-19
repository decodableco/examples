-- MySQL
CREATE DATABASE ecom;
USE ecom;

CREATE TABLE products (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(512),
  weight FLOAT,
  category VARCHAR(255) NOT NULL
);
ALTER TABLE products AUTO_INCREMENT = 101;

INSERT INTO products
VALUES (default,"scooter","Small 2-wheel scooter",3.14, "mobility"),
       (default,"car battery","12V car battery",8.1, "mobility"),
       (default,"12-pack drill bits","12-pack of drill bits with sizes ranging from #40 to #3",0.8, "home improvement"),
       (default,"hammer","12oz carpenter's hammer",0.75, "home improvement"),
       (default,"hammer","14oz carpenter's hammer",0.875, "home improvement"),
       (default,"hammer","16oz carpenter's hammer",1.0, "home improvement"),
       (default,"rocks","box of assorted rocks",5.3, "outdoor"),
       (default,"tent","three-person tent",105.0, "outdoor"),
       (default,"jacket","water resistent black wind breaker",0.1, "fashion"),
       (default,"spare tire","24 inch spare tire",22.2, "mobility");

CREATE TABLE customers (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE KEY,
  category ENUM('new customer', 'bronze', 'silver', 'gold', 'platinum')
) AUTO_INCREMENT=1001;

INSERT INTO customers
VALUES (default,"Sally","Thomas","sally.thomas@acme.com", "new customer"),
       (default,"George","Bailey","gbailey@foobar.com", "silver"),
       (default,"Edward","Walker","ed@walker.com", "gold"),
       (default,"Aidan","Barrett","aidan@example.com", "silver"),
       (default,"Anne","Kretchmar","annek@noanswer.org", "platinum"),
       (default,"Melissa","Cole","melissa@example.com", "gold"),
       (default,"Rosalie","Stewart","rosalie@example.com", "bronze");

CREATE TABLE purchase_orders (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_date DATE NOT NULL,
  purchaser_id INTEGER NOT NULL,
  FOREIGN KEY (purchaser_id) REFERENCES customers(id)
);
ALTER TABLE purchase_orders AUTO_INCREMENT = 10001;

INSERT INTO purchase_orders
VALUES (default, '2024-01-16', 1001),
       (default, '2024-01-17', 1002),
       (default, '2024-02-19', 1002),
       (default, '2024-02-11', 1004),
       (default, '2024-01-13', 1005),
       (default, '2024-03-17', 1006);

CREATE TABLE order_lines (
  id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
  order_id INTEGER NOT NULL,
  product_id INTEGER NOT NULL,
  quantity INTEGER NOT NULL,
  price NUMERIC(8, 2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES purchase_orders(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);
ALTER TABLE order_lines AUTO_INCREMENT = 100001;

INSERT INTO order_lines
VALUES (default, 10001, 102, 1, 39.99),
       (default, 10001, 105, 2, 129.99),
       (default, 10002, 106, 1, 29.49),
       (default, 10002, 107, 3, 49.49),
       (default, 10002, 103, 1, 19.49),
       (default, 10003, 101, 4, 219.99);
