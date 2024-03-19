## Startup

```bash
docker compose up -d 
```
****
## Result Mode
### table

```sql
-- This is the default; setting it just for clarity
SET 'sql-client.execution.result-mode' = 'table';
```

```sql
SELECT * FROM (
    VALUES ('Johnny'),
           ('Moira'),
           ('David'),
           ('David'),
           ('Johnny'));
```
### changelog

```sql
SET 'sql-client.execution.result-mode' = 'changelog';
```

```sql
SELECT * FROM (
    VALUES ('Johnny'),
           ('Moira'),
           ('David'),
           ('David'),
           ('Johnny'));
```
### tableau

```sql
SET 'sql-client.execution.result-mode' = 'tableau';
```

```sql
SELECT * FROM (
    VALUES ('Johnny'),
           ('Moira'),
           ('David'),
           ('David'),
           ('Johnny'));
```

## Runtime Mode

### streaming
```sql
-- Streaming is the default
SET 'execution.runtime-mode' = 'streaming';
```

```sql
SELECT name, COUNT(*) FROM (
    VALUES ('Johnny'), 
           ('Moira'), 
           ('David'), 
           ('David'), 
           ('Johnny')) AS tbl(name) 
    GROUP BY name;
```

### batch
```sql
SET 'execution.runtime-mode' = 'batch';
```

```sql
SELECT name, COUNT(*) FROM (
    VALUES ('Johnny'), 
           ('Moira'), 
           ('David'), 
           ('David'), 
           ('Johnny')) AS tbl(name) 
    GROUP BY name;
```


## Connectivity - Kafka

Send a message to Kafka topic
```bash
echo "foobar" | docker exec -i kcat kcat -b broker:29092 -P -t test_topic
```

Read it back

```bash
docker exec -i kcat kcat -b broker:29092 -C -t test_topic -u
```

Launch SQL Client
```bash
docker exec -it jobmanager bash -c "./bin/sql-client.sh"
```

Define a Flink SQL Table to read from the Kafka topic
```sql
CREATE TABLE t_k_test_topic (
  `msg` STRING
) WITH (
  'connector' = 'kafka',
  'topic' = 'test_topic',
  'properties.bootstrap.servers' = 'broker:29092',
  'scan.startup.mode' = 'earliest-offset',
  'format' = 'raw'
);
```

```sql
SET 'sql-client.execution.result-mode' = 'tableau';
```

```sql
SELECT * FROM t_k_test_topic;
```

```bash
echo "foobar again" | docker exec -i kcat kcat -b broker:29092 -P -t test_topic
```

```sql
insert into t_k_test_topic values ('Hello from the other side');
```

Read it back

```bash
docker exec -i kcat kcat -b broker:29092 -C -t test_topic -u
```

### Table configuration, e.g. Read from latest offset

```SQL
CREATE TABLE t_k_test_topic_latest (
  `col1` STRING
) WITH (
  'connector' = 'kafka',
  'topic' = 'test_topic',
  'properties.bootstrap.servers' = 'broker:29092',
  'scan.startup.mode' = 'latest-offset',
  'format' = 'raw'
);
```

```sql
select * from t_k_test_topic_latest;
```

## It's all DDL - Create an Iceberg table

```sql
CREATE TABLE t_iceberg_test (col1 STRING) 
  WITH (
  'connector' = 'iceberg',
  'catalog-type'='hive',
  'catalog-name'='dev',
  'warehouse' = 's3a://warehouse',
  'hive-conf-dir' = './conf');
```

```sql
INSERT INTO t_iceberg_test VALUES ('FOO');
```

```bash
docker exec mc bash -c \
        "mc ls -r minio/warehouse/"
```

```sql
select * from t_iceberg_test;
```

---

## Hook it together - Kafka to Iceberg with Transformation on the way

Start ShadowTraffic

```bash
docker compose up shadowtraffic
```

_If it fails with `Error response from daemon: network a503e9013494447fd2e862d7526a1fa8bfc913d66f1088d2face330f12619e07 not found` then clean up any existing shadowtraffic containers_

```bash
docker ps -a|grep shadowtraffic|awk '{print $1}'|xargs -Ifoo docker rm -f foo
docker compose up shadowtraffic
```

Confirm that there's data:

```bash
docker exec -i kcat kcat -b broker:29092 -C -t orders -c -U | jq '.'
```

```sql
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
```

```sql
SET 'pipeline.operator-chaining.enabled' = 'false';
SET 'execution.checkpointing.interval' = '60sec';
```

```sql
CREATE TABLE t_i_orders 
  WITH (
  'connector' = 'iceberg',
  'catalog-type'='hive',
  'catalog-name'='dev',
  'warehouse' = 's3a://warehouse',
  'hive-conf-dir' = './conf')
  AS 
  SELECT * FROM t_k_orders;
```

View the Flink dashboard: http://localhost:8081/#/overview

```bash
docker exec -it jobmanager bash -c "duckdb"
```

```sql
INSTALL httpfs;
INSTALL iceberg;
LOAD httpfs;
LOAD iceberg;
CREATE SECRET secret1 (
    TYPE S3,
    KEY_ID 'admin',
    SECRET 'password',
    REGION 'us-east-1',
    ENDPOINT 'minio:9000',
    URL_STYLE 'path',
    USE_SSL 'false'
);
```

```bash
docker exec mc bash -c \
        "mc ls -r minio/warehouse/" | grep orders | grep json | tail -n1 | \
        awk '{print "SELECT count(*), strftime(to_timestamp(max(create_ts)/1000),'\''%Y-%m-%d %H:%M:%S'\'') as max_ts, \n avg(cost), min(cost) \n FROM iceberg_scan('\''s3://warehouse/" $6"'\'');"}'
```

```sql
SELECT *
FROM iceberg_scan('s3://warehouse/default_database.db/t_i_orders/metadata/00000-9f24cff7-bd6c-42c5-9f5f-be4e3a94fd14.metadata.json')
```

****

## Appendix

### Catalog

```sql
 CREATE CATALOG c_hive WITH (
        'type' = 'hive',
        'hive-conf-dir' = './conf');
```

```sql
CREATE DATABASE c_hive.db01;
```

```sql
USE c_hive.db01;
```

```SQL
SHOW TABLES;
```

```SQL
CREATE TABLE c_hive.db01.t_k_test_topic (
  `col1` STRING
) WITH (
  'connector' = 'kafka',
  'topic' = 'test_topic',
  'properties.bootstrap.servers' = 'broker:29092',
  'scan.startup.mode' = 'earliest-offset',
  'format' = 'raw'
);
```

```sql
select * from c_hive.db01.t_k_test_topic;
```

### Iceberg catalog

```sql
CREATE CATALOG c_iceberg WITH (
       'type' = 'iceberg',
       'catalog-type'='hive',
       'warehouse' = 's3a://warehouse',
       'hive-conf-dir' = './conf');
```

```sql
CREATE DATABASE c_iceberg.dev;
```

```sql
USE c_iceberg.dev;
```

```sql
CREATE TABLE c_iceberg.dev.t_i_test AS 
  SELECT * FROM c_hive.db01.t_k_test_topic;
```


```sql
SHOW JOBS;
```

```sql
STOP JOB '6c9790735d4658d4ac9802961cd137b3';
```

```bash
docker exec mc bash -c "mc ls -r minio/warehouse/"
```

```bash
❯ docker exec mc bash -c \
        "mc cat minio/warehouse/db01.db/t_i_test/data/00000-0-5d37a1b3-de2b-4383-8711-17ddce00c993-00001.parquet" > /tmp/data.parquet && \
        duckdb :memory: "SELECT * FROM read_parquet('/tmp/data.parquet')"
-- Loading resources from /Users/rmoff/.duckdbrc
┌──────────────────┐
│       col1       │
│     varchar      │
├──────────────────┤
│ foobar           │
│ foobar again     │
│ foobar yet again │
└──────────────────┘

```

****

```sql
CREATE TABLE t_i_test WITH (
  'connector' = 'iceberg',
  'catalog-type'='hive',
  'catalog-name'='dev',
  'warehouse' = 's3a://warehouse',
  'hive-conf-dir' = './conf')
AS 
  SELECT * FROM t_k_test_topic;
```
  
```SQL
 CREATE TABLE foo (
     col1 string
 ) WITH (
   'connector' = 'datagen'
 );
```

```sql
INSERT INTO t_i_test
  SELECT * FROM foo;
```
