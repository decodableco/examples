SET search_path TO inventory;

DROP TABLE customers CASCADE;

CREATE TABLE customers (
  id SERIAL NOT NULL PRIMARY KEY,
  fname VARCHAR(255) NOT NULL,
  lname VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE,
  street VARCHAR(255),
  zip VARCHAR(255),
  city VARCHAR(255),
  phone VARCHAR(255),
  status INT,
  registered TIMESTAMP
);
ALTER SEQUENCE customers_id_seq RESTART WITH 1001;
ALTER TABLE customers REPLICA IDENTITY FULL;

CREATE TABLE phone_numbers (
  id SERIAL NOT NULL PRIMARY KEY,
  customer_id INT NOT NULL REFERENCES customers (id),
  preferred BOOLEAN,
  value VARCHAR(255)
);
ALTER SEQUENCE phone_numbers_id_seq RESTART WITH 10001;
ALTER TABLE phone_numbers REPLICA IDENTITY FULL;

INSERT INTO customers
VALUES (default, 'Brandon', 'Thomas', 'brandon.thomas@example.com', '29 Oakwood Drive', '90210', 'Los Angeles', '(800) 555‑2100', 1, '2023-02-17 21:36:42');

INSERT INTO customers
VALUES (default, 'Sarah', 'Davies', 'sarah.davies@example.com', '29 Oakwood Drive', '90210', 'Los Angeles', null, 1, '2023-02-17 21:36:42');
INSERT INTO phone_numbers
VALUES (default, 1002, true, '(800) 555‑2100');
INSERT INTO phone_numbers
VALUES (default, 1002, false, '(800) 555‑2200');
INSERT INTO phone_numbers
VALUES (default, 1002, false, '(800) 555‑2300');
