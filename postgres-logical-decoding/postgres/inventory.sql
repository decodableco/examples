--
--  Copyright 2023 The original authors
--
--  Licensed under the Apache License, Version 2.0 (the "License");
--  you may not use this file except in compliance with the License.
--  You may obtain a copy of the License at
--
--      http://www.apache.org/licenses/LICENSE-2.0
--
--  Unless required by applicable law or agreed to in writing, software
--  distributed under the License is distributed on an "AS IS" BASIS,
--  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
--  See the License for the specific language governing permissions and
--  limitations under the License.
--

CREATE SCHEMA inventory;
SET search_path TO inventory;

CREATE TABLE customer (
  id SERIAL NOT NULL PRIMARY KEY,
  first_name VARCHAR(255) NOT NULL,
  last_name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL UNIQUE
 
);
ALTER SEQUENCE customer_id_seq RESTART WITH 1001;
ALTER TABLE customer REPLICA IDENTITY FULL;

CREATE TABLE address (
  id SERIAL NOT NULL PRIMARY KEY,
  customer_id INTEGER NOT NULL REFERENCES customer(id),
  type VARCHAR(255) NOT NULL,
  line_1 VARCHAR(255) NOT NULL,
  line_2 VARCHAR(255),
  zip_code VARCHAR(255) NOT NULL,
  city VARCHAR(255) NOT NULL,
  country VARCHAR(255) NOT NULL
);
ALTER SEQUENCE address_id_seq RESTART WITH 10001;
ALTER TABLE address REPLICA IDENTITY FULL;
