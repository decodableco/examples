# Array Aggregation with Flink SQL

This demo shows how to aggregate the contents of arrays with Flink SQL, using the built-in function `JSON_ARRAYAGG()`, as well as a user-defined function for emitting a fully type-safe data structure.
It uses the [Upsert Kafka SQL Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/upsert-kafka/) together with the [Postgres CDC connector for Apache Flink](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/postgres-cdc.html), based on [Debezium](https://debezium.io/).
[Redpanda](https://redpanda.com/) is used as a data streaming platform.

## Prerequisites

Make sure to have the following software installed on your machine:

* Java and Apache Maven
* Docker and Docker Compose
* Redpanda's rpk CLI
* jq (optional)

## Preparation

Build the JAR with the `ARRAY_AGGR` operator:

```bash
mvn clean verify
```

Start up all the components using Docker Compose:

```bash
docker compose up --build
```

Obtain a Flink SQL prompt and enable mini-batching:

```bash
docker-compose run sql-client
```

```sql
SET 'table.exec.mini-batch.enabled' = 'true';
SET 'table.exec.mini-batch.allow-latency' = '500 ms';
SET 'table.exec.mini-batch.size' = '1000';
```

Obtain a Postgres client session:

```bash
docker run --tty --rm -i \
  --network array-agg-network \
  quay.io/debezium/tooling:1.2 \
  bash -c 'pgcli postgresql://postgres:postgres@postgres:5432/postgres'
```

Create two topics in Redpanda:

```bash
rpk topic create orders_with_lines orders_with_lines_and_customer
```

## Ingesting Data From Postgres

Create a table in Flink SQL for ingesting the data from the `orders` table in Postgres:

```sql
CREATE TABLE purchase_orders (
   id INT,
   order_date DATE,
   purchaser_id INT,
   db_name STRING METADATA FROM 'database_name' VIRTUAL,
   operation_ts TIMESTAMP_LTZ(3) METADATA FROM 'op_ts' VIRTUAL,
   PRIMARY KEY (id) NOT ENFORCED
) WITH (
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
```

Create a table for order lines:

```sql
CREATE TABLE order_lines (
   id INT,
   order_id INT,
   product_id INT,
   quantity INT,
   price DOUBLE,
   PRIMARY KEY (id) NOT ENFORCED
) WITH (
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
```

Register the UDF:

```sql
CREATE FUNCTION ARRAY_AGGR AS 'co.decodable.demos.arrayagg.ArrayAggr';
```

Perform some data changes in Postgres (via pgcli) and observe how the data in the Flink shell changes accordingly:

```sql
TODO
```

## Emitting Data to Redpanda

Create an instance of the Kafka upsert connector:

```sql
CREATE TABLE orders_with_lines (
  order_id INT,
  order_date DATE,
  purchaser_id INT,
  lines ARRAY<row<id INT, product_id INT, quantity INT, price DOUBLE>>,
  PRIMARY KEY (order_id) NOT ENFORCED
 )
WITH (
  'connector' = 'upsert-kafka',
  'topic' = 'orders-with-lines',
  'properties.bootstrap.servers' = 'redpanda:29092',
  'key.format' = 'json', 'value.format' = 'json'
);
```

```sql
INSERT INTO orders_with_lines
  SELECT
    po.id,
    po.order_date,
    po.purchaser_id,
    ARRAY_AGGR(ROW(ol.id, ol.product_id, ol.quantity, ol.price))
  FROM
    purchase_orders po
      LEFT JOIN order_lines ol ON ol.order_id = po.id
  GROUP BY po.id, po.order_date, po.purchaser_id;
```

Next, observe the data in Redpanda (do some more data changes in Postgres as well):

```bash
rpk topic consume orders-with-lines | jq '.value | fromjson'
```

## Emitting events to Elasticsearch

```sql
CREATE TABLE orders_with_lines_es (
  order_id INT,
  order_date DATE,
  purchaser_id INT,
  lines ARRAY<row<id INT, product_id INT, quantity INT, price DOUBLE>>,
  PRIMARY KEY (order_id) NOT ENFORCED
 )
 WITH (
     'connector' = 'elasticsearch-7',
     'hosts' = 'http://elasticsearch:9200',
     'index' = 'orders_with_lines'
 );
```

```bash
http http://localhost:9200/orders_with_lines/_doc/10001 | jq .
```

## Clean-up

Shut down all the components using Docker Compose:

```bash
docker compose down
```

http://localhost:9200/orders_with_lines/_search?pretty