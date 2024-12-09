# Transaction-aware Aggregation of CDC Events

This is an _experimental_ Flink job showing how to aggregate CDC events while respecting transactional boundaries in the source database. It's sample code accompanying the discussion in this  [blog post](https://www.decodable.co/blog).

## Infrastructure Setup 

There is a [Compose](./compose.yaml) file to locally spin up containers for the following systems by running `docker compose -f compose.yaml up` directly from within the `flink-datastream-poc` folder:

* MySQL as source database
* Apache Kafka
* Kafka Connect for running the Debezium Source Connector for MySQL

Checking with `docker ps` should thus show three containers being up and running fine:

```text
CONTAINER ID   IMAGE                                        COMMAND                  CREATED         STATUS                   PORTS                                        NAMES
8affed92db39   quay.io/debezium/connect:3.0.1.Final         "/docker-entrypoint.…"   2 minutes ago   Up 2 minutes             8778/tcp, 0.0.0.0:8083->8083/tcp, 9092/tcp   flink-tx-cdc-aggregation-connect-1
891a791834fc   quay.io/strimzi/kafka:0.43.0-kafka-3.8.0     "sh -c './bin/kafka-…"   2 minutes ago   Up 2 minutes             0.0.0.0:9092->9092/tcp                       flink-tx-cdc-aggregation-kafka-1
6f9a4e43243a   quay.io/debezium/example-mysql:3.0.1.Final   "docker-entrypoint.s…"   2 minutes ago   Up 2 minutes (healthy)   0.0.0.0:3306->3306/tcp, 33060/tcp            flink-tx-cdc-aggregation-mysql-1
```

## Set up Debezium MySQL Source Connector

The next step is to run an instance of the Debezium MySQL source connector in Kafka Connect which can be done by running the following connector configuration against the REST API of Connect like so:

```bash
curl --location 'localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data '{
    "name": "demo-connector-001", 
    "config": {
        "connector.class": "io.debezium.connector.mysql.MySqlConnector", 
        "database.hostname": "mysql", 
        "database.port": "3306", 
        "database.user": "root", 
        "database.password": "123456", 
        "database.server.id": "12345",
        "topic.prefix": "demodb", 
        "database.include.list": "inventory",
        "schema.history.internal.kafka.bootstrap.servers": "kafka:9092", 
        "schema.history.internal.kafka.topic": "schemahistory.demodb.inventory", 
        "include.schema.changes": "true",
        "provide.transaction.metadata": "true",
        "tombstones.on.delete": "false"
    }
}'
```

This configuration will capture any changes for all five tables which are found in the MySQL example database named `inventory`. Important is the configuration property `"provide.transaction.metadata": "true"` which instructs Debezium to expose all the necessary metadata related to transaction handling in the MySQL database. Without this additional information the transaction-aware aggregation of CDC events would not be possible.

## Execute Database Transaction in MySQL

To verify if the setup is working, the following simple transaction is executed in the `inventory` database which touches two tables, namely `customers` and the `addresses`:

Run this command to enter a CLI session inside the MySQL container:

```bash
docker compose exec mysql mysql -u root -p123456
```

Then inside the MySQL CLI run the transaction which inserts a new customer together with a single address record:

```sql
-- INSERTS 1 customer with 1 address in a transaction
START TRANSACTION;
INSERT INTO inventory.customers VALUES
    (default, 'Issac', 'Fletcher', 'ifletcher@example.com');
SET @customer_id = LAST_INSERT_ID();
INSERT INTO inventory.addresses VALUES
    (default, @customer_id, '1234 Nowhere Street', 'Great City', 'SomeState', '12345', 'LIVING');
COMMIT;
```

To verify the transaction-related metadata in the corresponding Kafka topic `demodb.transaction` run this command:

```bash
docker compose exec kafka ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic demodb.transaction --from-beginning
```

which should show two metadata events, one for each transaction marker:

* **BEGIN marker**

```json5
{
    "schema": {
       /* ... */
    },
    "payload": {
        "status": "BEGIN",
        "id": "file=binlog.000003,pos=236",
        "event_count": null,
        "data_collections": null,
        "ts_ms": 1733763172000
    }
}
```

* **END marker**

```json5
{
    "schema": {
      /* ... */
    },
    "payload": {
        "status": "END",
        "id": "file=binlog.000003,pos=236",
        "event_count": 2,
        "data_collections": [
            {
                "data_collection": "inventory.customers",
                "event_count": 1
            },
            {
                "data_collection": "inventory.addresses",
                "event_count": 1
            }
        ],
        "ts_ms": 1733763168000
    }
}
```

## Run Apache Flink Job

With all this in place it's time to build and run the Flink job and verify that the two CDC events which are part of this transaction are successfully aggregated.

_! NOTE: Building the experimental Flink code requires that you have JDK 17 and installed locally on your machine !_

From within the `flink-datastream-poc` folder run:

```bash
./mvnw clean package
```

which should result in a successful build of a self-contained JAR file that you can run as is:

```bash
java --add-opens=java.base/java.util=ALL-UNNAMED -jar target/flink-datastream-tx-buffering-1.0-SNAPSHOT.jar
```

With the job still running, you can switch into another terminal window to  verify that the resulting transactional buffer has been written into the Kafka output topic named `cdc.tx.buffers`:

```bash
docker compose exec kafka ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic cdc.tx.buffers --from-beginning
```

```json5
{
    "beginMarker": {
        "id": "file=binlog.000003,pos=236",
        "status": "BEGIN",
        "event_count": 0,
        "data_collections": null,
        "ts_ms": 1733763172000
    },
    "endMarker": {
        "id": "file=binlog.000003,pos=236",
        "status": "END",
        "event_count": 2,
        "data_collections": [
            {
                "data_collection": "inventory.customers",
                "event_count": 1
            },
            {
                "data_collection": "inventory.addresses",
                "event_count": 1
            }
        ],
        "ts_ms": 1733763168000
    },
    "buffer": {
        "inventory.customers": [
            {
                "key": "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"id\"}],\"optional\":false,\"name\":\"demodb.inventory.customers.Key\"},\"payload\":{\"id\":1005}}",
                "value": "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"first_name\"},{\"type\":\"string\",\"optional\":false,\"field\":\"last_name\"},{\"type\":\"string\",\"optional\":false,\"field\":\"email\"}],\"optional\":true,\"name\":\"demodb.inventory.customers.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"first_name\"},{\"type\":\"string\",\"optional\":false,\"field\":\"last_name\"},{\"type\":\"string\",\"optional\":false,\"field\":\"email\"}],\"optional\":true,\"name\":\"demodb.inventory.customers.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_us\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ns\"},{\"type\":\"string\",\"optional\":true,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"server_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"gtid\"},{\"type\":\"string\",\"optional\":false,\"field\":\"file\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"pos\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"row\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"thread\"},{\"type\":\"string\",\"optional\":true,\"field\":\"query\"}],\"optional\":false,\"name\":\"io.debezium.connector.mysql.Source\",\"field\":\"source\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"name\":\"event.block\",\"version\":1,\"field\":\"transaction\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_us\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ns\"}],\"optional\":false,\"name\":\"demodb.inventory.customers.Envelope\",\"version\":2},\"payload\":{\"before\":null,\"after\":{\"id\":1005,\"first_name\":\"Issac\",\"last_name\":\"Fletcher\",\"email\":\"ifletcher@example.com\"},\"source\":{\"version\":\"3.0.1.Final\",\"connector\":\"mysql\",\"name\":\"demodb\",\"ts_ms\":1733763168000,\"snapshot\":\"false\",\"db\":\"inventory\",\"sequence\":null,\"ts_us\":1733763168000000,\"ts_ns\":1733763168000000000,\"table\":\"customers\",\"server_id\":12345,\"gtid\":null,\"file\":\"binlog.000003\",\"pos\":383,\"row\":0,\"thread\":187,\"query\":null},\"transaction\":{\"id\":\"file=binlog.000003,pos=236\",\"total_order\":1,\"data_collection_order\":1},\"op\":\"c\",\"ts_ms\":1733763172327,\"ts_us\":1733763172327398,\"ts_ns\":1733763172327398676}}"
            }
        ],
        "inventory.addresses": [
            {
                "key": "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"id\"}],\"optional\":false,\"name\":\"demodb.inventory.addresses.Key\"},\"payload\":{\"id\":17}}",
                "value": "{\"schema\":{\"type\":\"struct\",\"fields\":[{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"customer_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"street\"},{\"type\":\"string\",\"optional\":false,\"field\":\"city\"},{\"type\":\"string\",\"optional\":false,\"field\":\"state\"},{\"type\":\"string\",\"optional\":false,\"field\":\"zip\"},{\"type\":\"string\",\"optional\":false,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"SHIPPING,BILLING,LIVING\"},\"field\":\"type\"}],\"optional\":true,\"name\":\"demodb.inventory.addresses.Value\",\"field\":\"before\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"int32\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"customer_id\"},{\"type\":\"string\",\"optional\":false,\"field\":\"street\"},{\"type\":\"string\",\"optional\":false,\"field\":\"city\"},{\"type\":\"string\",\"optional\":false,\"field\":\"state\"},{\"type\":\"string\",\"optional\":false,\"field\":\"zip\"},{\"type\":\"string\",\"optional\":false,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"SHIPPING,BILLING,LIVING\"},\"field\":\"type\"}],\"optional\":true,\"name\":\"demodb.inventory.addresses.Value\",\"field\":\"after\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"version\"},{\"type\":\"string\",\"optional\":false,\"field\":\"connector\"},{\"type\":\"string\",\"optional\":false,\"field\":\"name\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"ts_ms\"},{\"type\":\"string\",\"optional\":true,\"name\":\"io.debezium.data.Enum\",\"version\":1,\"parameters\":{\"allowed\":\"true,last,false,incremental\"},\"default\":\"false\",\"field\":\"snapshot\"},{\"type\":\"string\",\"optional\":false,\"field\":\"db\"},{\"type\":\"string\",\"optional\":true,\"field\":\"sequence\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_us\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ns\"},{\"type\":\"string\",\"optional\":true,\"field\":\"table\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"server_id\"},{\"type\":\"string\",\"optional\":true,\"field\":\"gtid\"},{\"type\":\"string\",\"optional\":false,\"field\":\"file\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"pos\"},{\"type\":\"int32\",\"optional\":false,\"field\":\"row\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"thread\"},{\"type\":\"string\",\"optional\":true,\"field\":\"query\"}],\"optional\":false,\"name\":\"io.debezium.connector.mysql.Source\",\"field\":\"source\"},{\"type\":\"struct\",\"fields\":[{\"type\":\"string\",\"optional\":false,\"field\":\"id\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"total_order\"},{\"type\":\"int64\",\"optional\":false,\"field\":\"data_collection_order\"}],\"optional\":true,\"name\":\"event.block\",\"version\":1,\"field\":\"transaction\"},{\"type\":\"string\",\"optional\":false,\"field\":\"op\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ms\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_us\"},{\"type\":\"int64\",\"optional\":true,\"field\":\"ts_ns\"}],\"optional\":false,\"name\":\"demodb.inventory.addresses.Envelope\",\"version\":2},\"payload\":{\"before\":null,\"after\":{\"id\":17,\"customer_id\":1005,\"street\":\"1234 Nowhere Street\",\"city\":\"Great City\",\"state\":\"SomeState\",\"zip\":\"12345\",\"type\":\"LIVING\"},\"source\":{\"version\":\"3.0.1.Final\",\"connector\":\"mysql\",\"name\":\"demodb\",\"ts_ms\":1733763168000,\"snapshot\":\"false\",\"db\":\"inventory\",\"sequence\":null,\"ts_us\":1733763168000000,\"ts_ns\":1733763168000000000,\"table\":\"addresses\",\"server_id\":12345,\"gtid\":null,\"file\":\"binlog.000003\",\"pos\":544,\"row\":0,\"thread\":187,\"query\":null},\"transaction\":{\"id\":\"file=binlog.000003,pos=236\",\"total_order\":2,\"data_collection_order\":1},\"op\":\"c\",\"ts_ms\":1733763172329,\"ts_us\":1733763172329898,\"ts_ns\":1733763172329898468}}"
            }
        ]
    }
}
```

And that's it! Feel free to try out other database transactions which could touch several rows spanning across any subset of the five tables found in the `inventory` database.
