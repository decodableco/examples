CREATE CATALOG c_iceberg_jdbc WITH (    'type' = 'iceberg',    'io-impl' = 'org.apache.iceberg.aws.s3.S3FileIO',    'warehouse' = 's3://warehouse',    's3.endpoint' = 'http://minio:9000',    's3.path-style-access' = 'true',    'catalog-impl' = 'org.apache.iceberg.jdbc.JdbcCatalog',    'uri' ='jdbc:postgresql://postgres:5432/?user=dba&password=rules');
CREATE DATABASE `c_iceberg_jdbc`.db01;
USE `c_iceberg_jdbc`.db01;
CREATE TABLE t_foo (c1 varchar, c2 int);
INSERT INTO t_foo VALUES ('a',42);