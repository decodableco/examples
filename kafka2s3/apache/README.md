# Installing Apache Kafka and sending data to S3


# Prepare RedHat

```bash
sudo yum update -y
sudo yum install -y java-11-openjdk
sudo yum install -y jq

```

# Download Apache Kafka

```bash
curl -O https://downloads.apache.org/kafka/3.2.1/kafka_2.13-3.2.1.tgz
tar xvf kafka_2.13-3.2.1.tgz 

cd kafka_2.13-3.2.1
```

# Configure Kafka
Open the Kafka broker configuration and set the listeners and advertised.listeners for remote accessing.

`vi config/server.properties`

```properties
# The address the socket server listens on. If not configured, the host name will be equal to the value of
# java.net.InetAddress.getCanonicalHostName(), with PLAINTEXT listener name, and port 9092.
#   FORMAT:
#     listeners = listener_name://host_name:port
#   EXAMPLE:
#     listeners = PLAINTEXT://your.host.name:9092
listeners=PLAINTEXT://0.0.0.0:9092,

# Listener name, hostname and port the broker will advertise to clients.
# If not set, it uses the value for "listeners".
advertised.listeners=PLAINTEXT://CHANGE-ME-TO-THE-PUBLIC-IP-OR-HOST:9092
```

# Start Kafka

```bash
./bin/zookeeper-server-start.sh -daemon config/zookeeper.properties
./bin/kafka-server-start.sh -daemon config/server.properties 
```

# Setup AWS RDS Postgres with Debezium / Enable CDC

Follow these commands for RDS Postgres: 
- https://github.com/debezium/debezium/blob/main/debezium-connector-postgres/RDS.md
- https://debezium.io/documentation/reference/stable/connectors/postgresql.html#postgresql-overview


When the parameter group is configured, restart Postgres and run the command below. The output should say `logical`.
```sql
SHOW wal_level;
```

A replication slot is a feature in PostgreSQL that ensures that the master server will retain the WAL logs that are needed by the replicas even when they are disconnected from the master.
```sql
SELECT * FROM pg_create_logical_replication_slot('pgoutput_rds', 'pgoutput')
```

Grant the user name used in the Postgres source connection.
```sql
grant rds_replication to $(PG_USER);
```

Then execute this commands in Postgres for confirmation.
```sql
select * from pg_replication_slots;
```

Execute the command below to enable CDC for each table.
```sql
ALTER TABLE $(my_table) REPLICA IDENTITY FULL;
```


