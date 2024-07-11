CREATE CATALOG c_delta WITH ( 'type' = 'delta-catalog', 'catalog-type' = 'in-memory');

CREATE DATABASE c_delta.db_new;

CREATE TABLE c_delta.db_new.t_foo (c1 varchar, c2 int)  WITH (  'connector' = 'delta',  'table-path' = 's3a://warehouse/t_foo');

INSERT INTO c_delta.db_new.t_foo 
    SELECT name, 42
    FROM (VALUES  ('Never'), ('Gonna'), ('Give'), ('You'), ('Up')) AS NameTable(name);