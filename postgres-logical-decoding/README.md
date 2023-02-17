# Logical Decoding Message Examples

Several examples demonstrating the usage of Postgres logical decoding messages,
as emitted using the [pg_logical_emit_message()](https://www.postgresql.org/docs/current/functions-admin.html#FUNCTIONS-REPLICATION) function.

## Requirements

* Java 11
* Docker

## Build

Run the following command to build this project:

```
mvn clean verify
```

Pass the `-Dquick` option to skip all non-essential plug-ins and create the output artifact as quickly as possible:

```
mvn clean verify -Dquick
```

Run the following command to format the source code and organize the imports as per the project's conventions:

```
mvn process-sources
```

This code base is available under the Apache License, version 2.

## Running the Examples

### Audit Logs

Start a Postgres database using the Docker Compose file in this directory:

```
docker compose up -d
```

Run a simple main class with a Flink job for enriching the change events:

```
mvn exec:exec@auditlogs
```

Start a Postgres client session:

```
docker run --tty --rm -i \
    --network logical-decoding-network \
    quay.io/debezium/tooling:1.2 \
    bash -c 'pgcli postgresql://postgresuser:postgrespw@postgres:5432/demodb'
```

In the Postgres client session run:

```
CREATE TABLE data(id serial primary key, data text);

BEGIN;
SELECT * FROM pg_logical_emit_message(true, 'audit', '{ "user" : "John", "client_ip" : "192.168.1.1" }');
INSERT INTO data(data) VALUES('aaa');
INSERT INTO data(data) VALUES('bbb');
COMMIT;
```

In the logs of the main class observe how the cange events for the two rows in the `data` table are enriched with the audit metadata provided via `pg_logical_emit_message()` as part of the same transaction before.

Stop the main class by pressing Ctrl + C and stop the database session by running Ctrl + D.
Shut down Postgres:

```
docker compose down
```

### Logging

Start a Postgres database using the Docker Compose file in this directory:

```
docker compose up -d
```

Start a Postgres client session:

```
docker run --tty --rm -i \
    --network logical-decoding-network \
    quay.io/debezium/tooling:1.2 \
    bash -c 'pgcli postgresql://postgresuser:postgrespw@postgres:5432/demodb'
```

In the Postgres client session run:

```
# Create a replication slot
SELECT * FROM pg_create_logical_replication_slot('demo_slot', 'test_decoding');

# A transaction that gets committed
BEGIN;
CREATE TABLE data (id INTEGER, value TEXT);
INSERT INTO data(id, value) VALUES('1', 'foo');
SELECT * FROM pg_logical_emit_message(false, 'log', 'OK');
INSERT INTO data(id, value) VALUES('2', 'bar');
COMMIT;

# A transaction that gets rolled back
BEGIN;
INSERT INTO data(id, value) VALUES('3', 'baz');
SELECT * FROM pg_logical_emit_message(false, 'log', 'ERROR');
INSERT INTO data(id, value) VALUES('4', 'qux');
ROLLBACK;

# Examine the changes from the replication slot
SELECT * FROM pg_logical_slot_peek_changes('demo_slot', NULL, NULL) order by lsn;
```

Shut down Postgres:

```
docker compose down
```

### Outbox

Start a Postgres database, Kafka and ZooKeeper using the Docker Compose file in this directory:

```
# Set the ADVERTISED_HOST_NAME env var of the Kafka container to your host system's IP
docker compose -f docker-compose-outbox.yml up -d
```

Run a simple main class with a Flink job for routing the outbox events:

```
mvn exec:exec@outbox
```

Start a Postgres client session:

```
docker run --tty --rm -i \
    --network logical-decoding-network \
    quay.io/debezium/tooling:1.2 \
    bash -c 'pgcli postgresql://postgresuser:postgrespw@postgres:5432/demodb'
```

In the Postgres client session run:

```
SELECT * FROM pg_logical_emit_message(
     true,
     'outbox',
     '{
         "id" : "298c2cc3-71bb-4d2b-b5b4-1b14006d56e6",
         "aggregate_type" : "shipment",
         "aggregate_id" : "42",
         "payload" : {
             "customer_id" : 7398,
             "item_id" : 8123,
             "status" : "SHIPPED",
             "numberOfPackages" : 3,
             "address" : "Bob Summers, 12 Main St., 90210, Los Angeles/CA, US"
         }
     }'
 );
```

Examine the routed event from the "shipment" Kafka topic:

```
docker run --tty --rm \
     --network logical-decoding-network \
     quay.io/debezium/tooling:1.2 \
     kcat -b kafka:9092 -C -o beginning -q -t shipment | jq .
```

Stop the main class and kcat by pressing Ctrl + C, and stop the database session by running Ctrl + D.
Shut down Postgres, Kafka and ZooKeeper:

```
docker compose -f docker-compose-outbox.yml down
```

### Advancing a Replication Slot

Start a Postgres database server with two databases using the Docker Compose file in this directory:

```
docker-compose -f docker-compose-multi-db.yml up -d
```

Run a simple main class with a Flink job for consuming change events from one of the databases:

```
mvn exec:exec@advanceslot
```

Start a Postgres client session for the other database and apply some data changes:

```
docker run --tty --rm -i \
    --network logical-decoding-network \
    quay.io/debezium/tooling:1.2 \
    bash -c 'pgcli postgresql://postgresuser:postgrespw@order-db:5432/db1'

postgresuser@order-db:db1> CREATE TABLE data (id INTEGER, value TEXT);
postgresuser@order-db:db1> INSERT INTO data SELECT generate_series(1,1000) AS id, md5(random()::text) AS value;
```

Query the replication slot and observe how its backlog grows:

```
SELECT
   slot_name,
   database,
   pg_size_pretty(
     pg_wal_lsn_diff(
       pg_current_wal_lsn(), restart_lsn)) AS retained_wal,
   active,
   restart_lsn, confirmed_flush_lsn FROM pg_replication_slots;
```

Get a session on the other database and emit a logical decoding message:

```
docker run --tty --rm -i \
    --network logical-decoding-network \
    quay.io/debezium/tooling:1.2 \
    bash -c 'pgcli postgresql://postgresuser:postgrespw@order-db:5432/db2'

postgresuser@order-db:db2> SELECT pg_logical_emit_message(false, 'heartbeat', now()::varchar);
```

Run the query above again, after a while the retained WAL size will be close to zero again.

Stop the main class by pressing Ctrl + C and stop the database sessions by running Ctrl + D.
Shut down Postgres:

```
docker compose -f docker-compose-multi-db.yml  down
```

## License

This code base is available under the Apache License, version 2.
