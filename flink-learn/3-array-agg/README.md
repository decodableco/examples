# Flink Kafka and Kafka Upsert Demo

This demo shows how to use Flink's [Apache Kafka SQL Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/kafka/) and the [Upsert Kafka SQL Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/upsert-kafka/) together with the [Postgres CDC connector for Apache Flink](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/postgres-cdc.html), based on [Debezium](https://debezium.io/).
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

And one for products:

```sql
CREATE TABLE products (
   id INT,
   name VARCHAR(255),
   description VARCHAR(512),
   weight DOUBLE,
   PRIMARY KEY (id) NOT ENFORCED
 ) WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'inventory',
   'table-name' = 'products',
   'slot.name' = 'products_slot'
 );
```

Perform some data changes in Postgres (via pgcli) and observe how the data in the Flink shell changes accordingly:

```sql
UPDATE shipments SET destination = 'Miami' WHERE shipment_id=1003;
INSERT INTO shipments VALUES (default, 10001, 'Los Angeles', 'New York City', false);
DELETE FROM shipments where shipment_id = 1004;
```

## Emitting Data to Kafka

It's vital to use the "kafka-upsert" connector:

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


CREATE FUNCTION ARRAY_AGGR AS 'co.decodable.demos.arrayagg.ArrayAggr';

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

In contrast, using the "kafka" connector won't work, as it cannot ingest the changelog stream emitted by the CDC connector:

```sql
CREATE TABLE shipments_output (
  shipment_id INT,
  order_id INT,
  origin STRING,
  destination STRING,
  is_arrived BOOLEAN,
  db_name STRING,
  operation_ts TIMESTAMP_LTZ(3)
)
WITH (
  'connector' = 'kafka',
  'topic' = 'shipments',
  'properties.bootstrap.servers' = 'redpanda:29092',
  'value.format' = 'json'
);
```

```sql
--this won't work
INSERT INTO shipments_output SELECT * FROM shipments;

[ERROR] Could not execute SQL statement. Reason:
org.apache.flink.table.api.TableException: Table sink 'default_catalog.default_database.shipments_output' doesn't support consuming update and delete changes which is produced by node TableSourceScan(table=[[default_catalog, default_database, shipments]], fields=[shipment_id, order_id, origin, destination, is_arrived, db_name, operation_ts])
```

Let's make those operations more easily visible:

```sql
SET 'sql-client.execution.result-mode' = 'changelog';
```

The "kafka" connector can only handle `I` (insert/append) events.

## Emitting Change Events

When emitting changes in the `debezium-json` format, the "kafka" connector can be used:

```sql
CREATE TABLE shipments_output_cdc (
  shipment_id INT,
  order_id INT,
  origin STRING,
  destination STRING,
  is_arrived BOOLEAN,
  db_name STRING,
  operation_ts TIMESTAMP_LTZ(3),
  PRIMARY KEY (shipment_id) NOT ENFORCED
 ) WITH (
  'connector' = 'kafka',
  'topic' = 'shipments-cdc',
  'properties.bootstrap.servers' = 'redpanda:29092',
  'format' = 'debezium-json'
 );

INSERT INTO shipments_output_cdc SELECT * FROM shipments;
```

```bash
rpk topic consume shipments-cdc | jq -c '.value | fromjson'
```

## Clean-up

Shut down all the components using Docker Compose:

```bash
docker compose down
```

http://localhost:9200/orders_with_lines/_search?pretty