# Flink Kafka and Kafka Upsert Demo

This demo shows how to use Flink's [Apache Kafka SQL Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/kafka/) and the [Upsert Kafka SQL Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/upsert-kafka/) together with the [Postgres CDC connector for Apache Flink](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/postgres-cdc.html), based on [Debezium](https://debezium.io/).
[Redpanda](https://redpanda.com/) is used as a data streaming platform.

## Prerequisites

Make sure to have the following software installed on your machine:

* Docker and Docker Compose
* Redpanda's rpk CLI
* jq (optional)

## Preparation

Start up all the components using Docker Compose:

```bash
docker compose up
```

Obtain a Flink SQL prompt:

```bash
docker-compose run sql-client
```

Obtain a Postgres client session:

```bash
docker run --tty --rm -i \
  --network upsert-network \
  quay.io/debezium/tooling:1.2 \
  bash -c 'pgcli postgresql://postgres:postgres@postgres:5432/postgres'
```

Create two topics in Redpanda:

```bash
rpk topic create shipments shipments-cdc
```

## Ingesting Data From Postgres

Create a table in Flink SQL for ingesting the data from the `shipments` table in Postgres:

```sql
CREATE TABLE shipments (
   shipment_id INT,
   order_id INT,
   origin STRING,
   destination STRING,
   is_arrived BOOLEAN,
   db_name STRING METADATA FROM 'database_name' VIRTUAL,
   operation_ts TIMESTAMP_LTZ(3) METADATA FROM 'op_ts' VIRTUAL,
   PRIMARY KEY (shipment_id) NOT ENFORCED
 ) WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'public',
   'table-name' = 'shipments'
 );
```

Display the contents of that table:

```sql
SELECT * FROM shipments;
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
CREATE TABLE shipments_output_upsert (
  shipment_id INT,
  order_id INT,
  origin STRING,
  destination STRING,
  is_arrived BOOLEAN,
  db_name STRING,
  operation_ts TIMESTAMP_LTZ(3),
  PRIMARY KEY (shipment_id) NOT ENFORCED
 )
WITH (
  'connector' = 'upsert-kafka',
  'topic' = 'shipments',
  'properties.bootstrap.servers' = 'redpanda:29092',
  'key.format' = 'json', 'value.format' = 'json'
);
```

```sql
INSERT INTO shipments_output_upsert SELECT * FROM shipments;
```

Next, observe the data in Redpanda (do some more data changes in Postgres as well):

```bash
rpk topic consume shipments | jq .
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
