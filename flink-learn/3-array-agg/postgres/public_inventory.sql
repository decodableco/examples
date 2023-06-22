SET search_path TO inventory;

DROP TABLE orders;

CREATE TABLE purchase_orders (
  id SERIAL NOT NULL PRIMARY KEY,
  order_date DATE NOT NULL,
  purchaser_id INTEGER NOT NULL,
  FOREIGN KEY (purchaser_id) REFERENCES customers(id)
);
ALTER SEQUENCE purchase_orders_id_seq RESTART WITH 10001;
ALTER TABLE purchase_orders REPLICA IDENTITY FULL;

INSERT INTO purchase_orders
VALUES (default, '2023-01-16', 1001),
       (default, '2023-01-17', 1002),
       (default, '2023-02-19', 1002),
       (default, '2023-06-21', 1003);

CREATE TABLE order_lines (
  id SERIAL NOT NULL PRIMARY KEY,
  order_id INTEGER NOT NULL,
  product_id INTEGER NOT NULL,
  quantity INTEGER NOT NULL,
  price NUMERIC(8, 2) NOT NULL,
  FOREIGN KEY (order_id) REFERENCES purchase_orders(id),
  FOREIGN KEY (product_id) REFERENCES products(id)
);
ALTER SEQUENCE order_lines_id_seq RESTART WITH 100001;
ALTER TABLE order_lines REPLICA IDENTITY FULL;

INSERT INTO order_lines
VALUES (default, 10001, 102, 1, 39.99),
       (default, 10001, 105, 2, 129.99),
       (default, 10002, 106, 1, 29.49),
       (default, 10002, 107, 3, 49.49),
       (default, 10002, 103, 1, 19.49),
       (default, 10003, 101, 4, 219.99);

CREATE TABLE customer_phone_numbers (
  id SERIAL NOT NULL PRIMARY KEY,
  customer_id INTEGER NOT NULL,
  type VARCHAR(20),
  value VARCHAR(100),
  FOREIGN KEY (customer_id) REFERENCES customers(id)
);
ALTER SEQUENCE customer_phone_numbers_id_seq RESTART WITH 1000001;
ALTER TABLE customer_phone_numbers REPLICA IDENTITY FULL;

INSERT INTO customer_phone_numbers
VALUES (default, 1001, 'home', '001'),
       (default, 1001, 'work', '002');
