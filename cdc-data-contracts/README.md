# Data Contracts For Change Data Capture

This demo shows how to use Flink SQL for creating a stable data contract for a CDC stream coming from a Postgres database,
using the [Postgres CDC connector for Apache Flink](https://ververica.github.io/flink-cdc-connectors/master/content/connectors/postgres-cdc.html)
(based on [Debezium](https://debezium.io/)),
Flink's [Upsert Kafka SQL Connector](https://nightlies.apache.org/flink/flink-docs-master/docs/connectors/table/upsert-kafka/),
and [Redpanda](https://redpanda.com/) is used as a data streaming platform.
It accompanies the blog post [“Change Data Capture Breaks Encapsulation”. Does it, though?](https://www.decodable.co/blog/change-data-capture-breaks-encapsulation-does-it-though).

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

Obtain a Postgres client session:

```bash
docker run --tty --rm -i \
  --network cdc-data-contracts-network \
  quay.io/debezium/tooling:1.2 \
  bash -c 'pgcli postgresql://postgres:postgres@postgres:5432/postgres'
```

```sql
SET search_path TO 'inventory';
```

Obtain a Flink SQL prompt:

```bash
docker-compose run sql-client

SET execution.checkpointing.interval = 10s;
```

Create a topic in Redpanda:

```bash
rpk topic create customers
```

## Ingesting Data From Postgres

Create a table in Flink SQL for ingesting the data from the `customers` table in Postgres:

```sql
CREATE TABLE customers (
   id INT,
   fname STRING,
   lname STRING,
   email STRING,
   street STRING,
   zip STRING,
   city STRING,
   phone STRING,
   status INT,
   registered TIMESTAMP,
   PRIMARY KEY (id) NOT ENFORCED
 ) WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'inventory',
   'table-name' = 'customers',
   'slot.name' = 'customers_replication_slot',
   'decoding.plugin.name' = 'pgoutput'
 );
```

Create a table for publishing the public data stream to:

```sql
CREATE TABLE customers_public (
  id INT,
  first_name STRING,
  last_name STRING,
  email STRING,
  zip STRING,
  phone STRING,
  status STRING,
  registration_date STRING,
  PRIMARY KEY (id) NOT ENFORCED
 )
WITH (
  'connector' = 'upsert-kafka',
  'topic' = 'customers',
  'properties.bootstrap.servers' = 'redpanda:29092',
  'key.format' = 'json', 'value.format' = 'json'
);
```

Create a job for transforming the internal CDC stream into the published format:

```sql
INSERT INTO customers_public
  SELECT 
    id,
    fname AS first_name,
    lname AS last_name,
    email,
    zip,
    phone,
    CASE
      WHEN status = 1 THEN 'NEW'
      WHEN status = 2 THEN 'VIP'
      WHEN status = 3 THEN 'BLOCKED'
      ELSE 'STANDARD'
    END,
    DATE_FORMAT(registered, 'dd-MM-yyyy')
  FROM customers;
```

Perform some data changes in Postgres (via pgcli) and observe how the published data change events show up in the Redpanda topic accordingly:

```sql
INSERT INTO customers
VALUES (default,'Saundra','Geoff', 'saundra.geoff@example.com', '29 Oakwood Drive', '90210', 'Los Angeles', '(800) 555‑3100', 1, '2023-02-17 21:36:42');
```

```bash
rpk topic consume customers | jq .
```
 
## Schema Changes

### Column Rename

This shows how to deal with renaming a column (`fname` to `first_name`) in the source table, without exposing this to public consumers.

Stop the Flink job with a savepoint:

```sql
SET 'state.savepoints.dir' = '/tmp/savepoints';
STOP JOB '<id>' WITH SAVEPOINT;
```

Issue a data change in Postgres:

```sql
UPDATE customers SET fname = 'Brandon Junior' WHERE id = 1001;
```

Rename the `fname` column:

```sql
ALTER TABLE customers RENAME COLUMN fname TO first_name;
```

Do another data change:

```sql
UPDATE customers SET first_name = 'Brandon III' WHERE id = 1001;
```

In Flink, add the `first_name` column and configure the savepoint path:

```sql
ALTER TABLE customers ADD first_name STRING;
SET 'execution.savepoint.path' = '/tmp/savepoints/savepoint-<job id>';
```

Deploy a new version of the job, retrieving the first name either from the `fname` or `first_name` column, depending on which one is present:

```sql
INSERT INTO customers_public
  SELECT 
    id,
    COALESCE(fname, first_name),
    lname,
    email,
    zip,
    phone,
    CASE
      WHEN status = 1 THEN 'NEW'
      WHEN status = 2 THEN 'VIP'
      WHEN status = 3 THEN 'BLOCKED'
      ELSE 'STANDARD'
    END,
    DATE_FORMAT(registered, 'dd-MM-yyyy')
  FROM customers;
```

Observe in the Redpanda topic how the two change events for the updates done while the job wasn't running show up.

## Misc.

### Exposing Additional Phone Numbers Of a Customer

Create a new table `phone_numbers` in Flink SQL:

```sql
CREATE TABLE phone_numbers (
   id INT,
   customer_id INT,
   `value` STRING,
   preferred BOOLEAN,
   PRIMARY KEY (id) NOT ENFORCED
 ) WITH (
   'connector' = 'postgres-cdc',
   'hostname' = 'postgres',
   'port' = '5432',
   'username' = 'postgres',
   'password' = 'postgres',
   'database-name' = 'postgres',
   'schema-name' = 'inventory',
   'table-name' = 'phone_numbers',
   'slot.name' = 'phone_numbers_replication_slot',
   'decoding.plugin.name' = 'pgoutput'
 );
```

Add a new field to the public change event stream in Flink SQL:

```sql
ALTER TABLE customers_public ADD further_phones ARRAY<STRING>;
```

Join the two source tables:

```sql
INSERT INTO customers_public
  SELECT
    c.id,
    c.fname,
    c.lname,
    c.email,
    c.zip,
    COALESCE(c.phone, preferred.`value`),
    CASE
      WHEN status = 1 THEN 'NEW'
      WHEN status = 2 THEN 'VIP'
      WHEN status = 3 THEN 'BLOCKED'
      ELSE 'STANDARD'
    END AS status,
    DATE_FORMAT(registered, 'dd-MM-yyyy'),
    JSON_ARRAYAGG(further_phones.`value`)
  FROM
    customers c
    LEFT JOIN
      (SELECT * FROM phone_numbers WHERE preferred = true) preferred
      ON c.id = preferred.customer_id
    LEFT JOIN
      (SELECT * FROM phone_numbers WHERE preferred = false) further_phones
      ON c.id = further_phones.customer_id
  GROUP BY
    c.id, fname, lname, email, zip, c.phone, registered, status, preferred.`value`;
```

## Clean-up

Shut down all the components using Docker Compose:

```bash
docker compose down
```
