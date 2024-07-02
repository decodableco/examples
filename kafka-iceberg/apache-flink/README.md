# Streaming data from Kafka to Iceberg with Apache Flink

_👉 See the supporting blog post at https://www.decodable.co/blog/_

## Run it all

The end-to-end example does the following:

* Brings up a Flink cluster, Kafka broker, and MinIO object store
* Generates dummy data to the Kafka `orders` topic
* Uses Flink SQL to write the Kafka `orders` topic to a table in Iceberg format on MinIO

_NB. test data is generated using [ShadowTraffic](https://shadowtraffic.io/). You can get a free trial licence—put your `license.env` file in the `shadowtraffic` folder. If you don't want to use ShadowTraffic you can insert your own dummy data on a Kafka topic._

```bash
# Bring up the stack
docker compose up

# Once launched, run this:
docker compose exec -it jobmanager bash -c "./bin/sql-client.sh -f /data/kafka-to-iceberg.sql"

# Check for data (should see a mix of parquet, json, and avro files under default_database.db/t_i_orders):
docker compose exec mc bash -c \
        "mc ls -r minio/warehouse/"
```

Check the data in DuckDB

1. Build a query using the latest manifest

    ```bash
    docker exec mc bash -c \
            "mc ls -r minio/warehouse/" | grep orders | grep json | tail -n1 | \
            awk '{print "SELECT count(*), strftime(to_timestamp(max(create_ts)/1000),'\''%Y-%m-%d %H:%M:%S'\'') as max_ts, \n avg(cost), min(cost) \n FROM iceberg_scan('\''s3://warehouse/" $6"'\'');"}'
    ```

2. Run it

    ```sql
    ⚫◗ SELECT count(*), strftime(to_timestamp(max(create_ts)/1000),'%Y-%m-%d %H:%M:%S') as max_ts,
        avg(cost), min(cost)
        FROM iceberg_scan('s3://warehouse/default_database.db/t_i_orders/metadata/00002-46e26aab-b843-4d45-aa2d-66804870a39e.metadata.json');
    ┌──────────────┬─────────────────────┬───────────────────┬─────────────┐
    │ count_star() │       max_ts        │    avg("cost")    │ min("cost") │
    │    int64     │       varchar       │      double       │    float    │
    ├──────────────┼─────────────────────┼───────────────────┼─────────────┤
    │           37 │ 2024-06-28 16:40:46 │ 115.5715142327386 │  100.209236 │
    └──────────────┴─────────────────────┴───────────────────┴─────────────┘
    ```

## Step-by-step

### Set up Kafka source

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
SELECT * FROM t_k_orders LIMIT 10;
```


```
                                                                     SQL Query Result (Table)
 Refresh: 1 s                                                            Page: Last of 1                                                    Updated: 15:20:43.336

                        orderId                     customerId orderNumber                        product backordered                           cost
 89a3bf3e-12e5-4386-ff1e-2de88~ f2e72581-d19b-6253-aa52-ce57f~           0      Intelligent Granite Chair       FALSE                      130.58978 Blanditiis qu
 fb9b04bd-d1a5-43dc-fa90-1ed75~ 5d3f2d00-8715-7b8d-1abd-7db76~           1            Mediocre Silk Bench       FALSE                       79.25486 Fuga reprehen
 4fee16e6-1326-6aa6-2a8a-b3919~ 21df0e3c-5e43-cd00-1c3e-258d0~           2      Aerodynamic Aluminum Coat       FALSE                       83.89926 Possimus labo
 de7c84ca-8b7b-c13a-4fb8-16179~ b1303f73-5ce4-7da3-ab41-d9b04~           3        Gorgeous Plastic Bottle       FALSE                      140.99934 Vero explicab
 67ab9269-b0ff-7e7f-deba-eaf8a~ 5d3f2d00-8715-7b8d-1abd-7db76~           4          Practical Plastic Hat       FALSE                        86.2369 Placeat nemo
 3e9b5fc5-d5d6-62b4-3abb-244f0~ b1303f73-5ce4-7da3-ab41-d9b04~           5          Fantastic Granite Hat       FALSE                      106.13418 Quod numquam
 58af6095-3aa5-eca8-c00c-98dfa~ 6878a7d0-1bb4-5817-485a-c6b85~           6              Gorgeous Iron Bag       FALSE                       94.56349 Dolorem magna
 0562400e-7b51-ccbb-85a1-09349~ 5d3f2d00-8715-7b8d-1abd-7db76~           7              Enormous Silk Hat       FALSE                      106.08421 Fugit omnis l
 2d772926-979d-d054-5bb0-e867f~ 6878a7d0-1bb4-5817-485a-c6b85~           8         Heavy Duty Bronze Lamp       FALSE                       67.12055 Nobis tempori
 b76e8915-7922-cd3b-0486-f4a42~ fd11ce95-358b-c682-994c-27246~           9        Intelligent Linen Watch       FALSE                      103.01574 Consequatur v

```

### Set up Iceberg sink

Set checkpoint to happen every minute

```sql
SET 'execution.checkpointing.interval' = '60sec';
```

Set this so that the operators are separate in the Flink WebUI.

```sql
SET 'pipeline.operator-chaining.enabled' = 'false';
```

Create Iceberg table

```sql
CREATE TABLE t_i_orders 
  WITH (
  'connector' = 'iceberg',
  'catalog-type'='hive',
  'catalog-name'='dev',
  'warehouse' = 's3a://warehouse',
  'hive-conf-dir' = './conf')
  AS 
  SELECT * FROM t_k_orders 
   WHERE cost > 100;
```

### Examine the data in MinIO

Check data:

```bash
❯ docker exec mc bash -c \
        "mc ls -r minio/warehouse/"
[2024-06-28 15:23:45 UTC] 6.3KiB STANDARD default_database.db/t_i_orders/data/00000-0-131b86c6-f4fc-4f26-9541-674ec3101ea8-00001.parquet
[2024-06-28 15:22:55 UTC] 2.0KiB STANDARD default_database.db/t_i_orders/metadata/00000-59d5c01b-1ab2-457b-9365-bf1cd056bf1d.metadata.json
[2024-06-28 15:23:47 UTC] 3.1KiB STANDARD default_database.db/t_i_orders/metadata/00001-5affbf21-7bb7-4360-9d65-d547211d63ab.metadata.json
[2024-06-28 15:23:46 UTC] 7.2KiB STANDARD default_database.db/t_i_orders/metadata/6bf97c2e-0e10-410f-8db8-c6cc279e3475-m0.avro
[2024-06-28 15:23:46 UTC] 4.1KiB STANDARD default_database.db/t_i_orders/metadata/snap-3773022978137163897-1-6bf97c2e-0e10-410f-8db8-c6cc279e3475.avro
```

### Look at the data with PyIceberg

```bash
docker compose exec pyiceberg "bash"
```

```bash
root@3e3ebb9c0be1:/# pyiceberg list
default
default_database

root@3e3ebb9c0be1:/# pyiceberg list default_database
default_database.t_i_orders

root@3e3ebb9c0be1:/# pyiceberg describe default_database.t_i_orders
Table format version  2
Metadata location     s3a://warehouse/default_database.db/t_i_orders/metadata/00010-e7d5499e-f73c-4ff3-a036-f17f644ac1ca.metadata.json
Table UUID            72b165e4-11f9-4a75-8a1b-e1bbfde06bae
Last Updated          1719842483459
Partition spec        []
Sort order            []
Current schema        Schema, id=0
                      ├── 1: orderId: optional string
                      ├── 2: customerId: optional string
                      ├── 3: orderNumber: optional int
                      ├── 4: product: optional string
                      ├── 5: backordered: optional boolean
                      ├── 6: cost: optional float
                      ├── 7: description: optional string
                      ├── 8: create_ts: optional long
                      ├── 9: creditCardNumber: optional string
                      └── 10: discountPercent: optional int
Current snapshot      Operation.APPEND: id=9116831331988708639, parent_id=9098627110859091234, schema_id=0
Snapshots             Snapshots
                      ├── Snapshot 5681413802900792746, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-5681413802900792746-1-f7670cb3-af47-478d-a90a-0b4e0074aabe.avro
                      ├── Snapshot 3079059435875923863, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-3079059435875923863-1-42e24305-3c5f-4eea-9df3-2bf529704740.avro
                      ├── Snapshot 1110224315320183294, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-1110224315320183294-1-08ba7134-ab55-4ae2-995f-085f83b62a05.avro
                      ├── Snapshot 5859436771394135890, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-5859436771394135890-1-8c1bbf78-3f8e-4d7e-b444-107874a29360.avro
                      ├── Snapshot 8505813483884320524, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-8505813483884320524-1-3f2f0738-67a2-4807-8565-dedd67cddb12.avro
                      ├── Snapshot 4956548979990641944, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-4956548979990641944-1-e669a94c-805c-4f85-89d3-3be3bad231f9.avro
                      ├── Snapshot 2916878419900541694, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-2916878419900541694-1-c39affb0-81b0-4f37-93be-198651dcd432.avro
                      ├── Snapshot 2521637909894096219, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-2521637909894096219-1-fa225a5f-a609-4844-95e6-6ccf16bb32f0.avro
                      ├── Snapshot 9098627110859091234, schema 0:
                      │   s3a://warehouse/default_database.db/t_i_orders/metadata/snap-9098627110859091234-1-a76147f2-4162-46df-968e-5192fbf6edaf.avro
                      └── Snapshot 9116831331988708639, schema 0:
                          s3a://warehouse/default_database.db/t_i_orders/metadata/snap-9116831331988708639-1-022a8006-ae0c-48c1-a61c-de9f3ca8daee.avro
Properties            hive-conf-dir                    ./conf
                      connector                        iceberg
                      write.parquet.compression-codec  zstd
                      catalog-type                     hive
                      catalog-name                     dev
                      warehouse                        s3a://warehouse
root@3e3ebb9c0be1:/#
```

### Use DuckDB to query the data

Look at the data with duckdb

```bash
docker exec -it jobmanager bash -c "duckdb"
```

Install the needful and configure S3/Minio connection

```sql
.prompt '⚫◗ '
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

Run this bash to generate a DuckDB SQL statement to query the latest version of the Iceberg table (https://duckdb.org/docs/guides/import/s3_iceberg_import#loading-iceberg-tables-from-s3[ref])

```bash
docker exec mc bash -c \
        "mc ls -r minio/warehouse/" | grep orders | grep json | tail -n1 | \
        awk '{print "SELECT count(*) AS row_ct, strftime(to_timestamp(max(create_ts)/1000),'\''%Y-%m-%d %H:%M:%S'\'') AS max_ts, \n AVG(cost) AS avg_cost, MIN(cost) AS min_cost \n FROM iceberg_scan('\''s3://warehouse/" $6"'\'');"}'
```

```sql
⚫◗ SELECT count(*), strftime(to_timestamp(max(create_ts)/1000),'%Y-%m-%d %H:%M:%S') as max_ts,
     avg(cost), min(cost)
     FROM iceberg_scan('s3://warehouse/default_database.db/t_i_orders/metadata/00001-5affbf21-7bb7-4360-9d65-d547211d63ab.metadata.json');
┌──────────────┬─────────────────────┬────────────────────┬─────────────┐
│ count_star() │       max_ts        │    avg("cost")     │ min("cost") │
│    int64     │       varchar       │       double       │    float    │
├──────────────┼─────────────────────┼────────────────────┼─────────────┤
│           23 │ 2024-06-28 15:11:11 │ 119.38902548085089 │   103.01574 │
└──────────────┴─────────────────────┴────────────────────┴─────────────┘
```

Wait for next checkpoint, get latest manifest for the Iceberg table: 

```bash
docker exec mc bash -c \
        "mc ls -r minio/warehouse/" | grep orders | grep json | tail -n1 | \
        awk '{print "SELECT count(*), strftime(to_timestamp(max(create_ts)/1000),'\''%Y-%m-%d %H:%M:%S'\'') as max_ts, \n avg(cost), min(cost) \n FROM iceberg_scan('\''s3://warehouse/" $6"'\'');"}'
```

Run it to see the changed data:

```sql
⚫◗ SELECT count(*), strftime(to_timestamp(max(create_ts)/1000),'%Y-%m-%d %H:%M:%S') as max_ts,
     avg(cost), min(cost)
     FROM iceberg_scan('s3://warehouse/default_database.db/t_i_orders/metadata/00003-36444b19-3cd6-4c06-ab77-b05e14af40c5.metadata.json');
┌──────────────┬─────────────────────┬───────────────────┬─────────────┐
│ count_star() │       max_ts        │    avg("cost")    │ min("cost") │
│    int64     │       varchar       │      double       │    float    │
├──────────────┼─────────────────────┼───────────────────┼─────────────┤
│           43 │ 2024-06-28 15:35:18 │ 117.6643137377362 │   100.03383 │
└──────────────┴─────────────────────┴───────────────────┴─────────────┘
```

## Smoke-Testing Flink Dependencies for Iceberg (with Hive metastore)

Test table, dummy source

```sql
CREATE TABLE iceberg_test WITH (
    'connector' = 'iceberg',
    'catalog-type'='hive',
    'catalog-name'='dev',
    'warehouse' = 's3a://warehouse',
    'hive-conf-dir' = './conf')
AS 
    SELECT name, COUNT(*) AS cnt 
    FROM (VALUES ('Never'), ('Gonna'), ('Give'), ('You'), ('Up')) AS NameTable(name) 
    GROUP BY name;
```

## Changing Iceberg table config

https://iceberg.apache.org/docs/1.5.2/configuration/#write-properties

e.g. `'write.format.default'='orc'`

```sql
CREATE TABLE iceberg_test WITH (
    'connector' = 'iceberg',
    'catalog-type'='hive',
    'catalog-name'='dev',
    'warehouse' = 's3a://warehouse',
    'hive-conf-dir' = './conf',
    'write.format.default'='orc')
AS 
    SELECT name, COUNT(*) AS cnt 
    FROM (VALUES ('Never'), ('Gonna'), ('Give'), ('You'), ('Up')) AS NameTable(name) 
    GROUP BY name;
```

```bash
❯ docker exec mc bash -c \
        "mc ls -r minio/warehouse/"
[2024-07-01 10:41:49 UTC]   398B STANDARD default_database.db/iceberg_test/data/00000-0-023674bd-dc7d-4249-8c50-8c1238881e57-00001.orc
[2024-07-01 10:41:44 UTC] 1.2KiB STANDARD default_database.db/iceberg_test/metadata/00000-bf7cc294-fe04-4e2d-af8b-722e20cfca97.metadata.json
[2024-07-01 10:41:50 UTC] 2.4KiB STANDARD default_database.db/iceberg_test/metadata/00001-0f8296eb-8e0b-4c0b-b7ab-c3bbbbcf2ff9.metadata.json
[2024-07-01 10:41:49 UTC] 6.5KiB STANDARD default_database.db/iceberg_test/metadata/279b6a97-ac90-492c-bbe7-7514af4f2a36-m0.avro
[2024-07-01 10:41:50 UTC] 4.1KiB STANDARD default_database.db/iceberg_test/metadata/snap-2795270994728078488-1-279b6a97-ac90-492c-bbe7-7514af4f2a36.avro
```

Change existing table: 

```sql
Flink SQL> ALTER TABLE iceberg_test SET ('write.format.default'='avro');
[INFO] Execute statement succeed.
```

or reset it to its default value:

```sql
Flink SQL> ALTER TABLE iceberg_test RESET ('write.format.default');
[INFO] Execute statement succeed.
```