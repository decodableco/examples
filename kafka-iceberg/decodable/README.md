# Streaming data from Kafka to Iceberg using Decodable

## Pre-requisites

1. [ShadowTraffic license key](https://docs.shadowtraffic.io/quickstart/#create-a-license-file) (free). Write the `license.env` file as detailed in the [instructions](https://docs.shadowtraffic.io/quickstart/#create-a-license-file) and store it under the `shadowtraffic/` folder.

2. [ngrok API key](https://dashboard.ngrok.com/signup) (free). Store this as a `.env` file in this folder:

        NGROK_AUTH_TOKEN=xxxxxxx

    TIP: Read [this article](https://rmoff.net/2023/11/01/using-apache-kafka-with-ngrok/) for information about running ngrok locally for serving Kafka to a remote client, and be aware of [this possible issue with DNS and ngrok](https://rmoff.net/2024/05/03/ngrok-dns-headaches/).

## Create Kafka connection in Decodable to ingest basket data

Data is written to Kafka from Shadowtraffic.

```bash
$ docker compose up
```

Get host/ip of broker

```bash
$ curl -s localhost:4040/api/tunnels | jq -r '.tunnels[0].public_url' | sed 's/tcp:\/\///g'
```

```bash
0.tcp.eu.ngrok.io:17956
```

Create connection (update the `bootstrap-servers` based on your Kafka broker, e.g. `0.tcp.eu.ngrok.io:17956` if using ngrok as above)

```bash
decodable connection create                                \
    --name kafka-basket                                    \
    --type source                                          \
    --connector kafka                                      \
    --prop bootstrap.servers=0.tcp.eu.ngrok.io:17956       \
    --prop value.format=json                               \
    --prop key.fields=basketId                             \
    --prop key.format=json                                 \
    --prop parse-error-policy=FAIL                         \
    --prop properties.auto.offset.reset=none               \
    --prop scan.startup.mode=earliest-offset               \
    --prop topic=supermarketBaskets                        \
    --prop value.fields-include=EXCEPT_KEY                 \
    --field basketId="STRING"                              \
    --field customerId="STRING"                            \
    --field customerName="STRING"                          \
    --field customerAddress="STRING"                       \
    --field storeId="STRING"                               \
    --field storeName="STRING"                             \
    --field storeLocation="STRING"                         \
    --field products="ARRAY<ROW( productName STRING, quantity INT, unitPrice FLOAT, category STRING )>"  \
    --field timestamp="STRING"
```

```
Created connection kafka-basket (4cc241e6)
```

Start the connection

```bash
decodable connection activate $(decodable query --name kafka-basket --keep-ids | yq '.metadata.id')
```

Check its status

```bash
decodable query --name kafka-basket --no-spec
```

```
---
kind: connection
metadata:
    name: kafka-basket
spec_version: v1
status:
    create_time: 2024-05-09T16:22:21.733+00:00
    update_time: 2024-05-09T16:22:21.733+00:00
    target_state: RUNNING
    actual_state: STARTING
    requested_tasks: 1
    actual_tasks: 1
    requested_task_size: M
    actual_task_size: M
    last_runtime_error:
        message: ""
        raw_exception: ""
        timestamp: null
    last_activated_time: 2024-05-09T16:25:48.876+00:00
```

Check the data

```bash
decodable stream preview --count 1 $(decodable query --keep-ids --name $(decodable query --name kafka-basket | yq '.spec.stream_name') | yq '.metadata.id') | jq '.' 
```

```
Records received:      1
{
  "basketId": "299fee47-e935-7979-dae0-f2614bc986ec",
  "customerId": "f2ce5720-6308-bedd-8e34-c1540fd0386b",
  "products": [
    {
      "productId": "f21297f4-f240-ba7a-f028-5dfe2d2b132a",
      "quantity": 3,
      "unitPrice": 6
    },
    {
      "productId": "86c27500-0731-afe7-e03b-c538cfa198e6",
      "quantity": 2,
      "unitPrice": 94
    },
    {
      "productId": "1415f12e-26c8-5805-b5da-09fc2cbf442e",
      "quantity": 4,
      "unitPrice": 98
    },
    {
      "productId": "3de8bc56-e2d2-b8a6-f6e5-9f3c57c2ee1e",
      "quantity": 2,
      "unitPrice": 95
    },
    {
      "productId": "5d19183b-1080-d234-f07d-0b4474ff4090",
      "quantity": 4,
      "unitPrice": 39
    },
    {
      "productId": "04e7416f-e27c-9aef-3c8a-3b819de0dfbc",
      "quantity": 5,
      "unitPrice": 80
    }
  ],
  "storeId": "280734ba-7a71-f250-114f-3602e058fe2a",
  "timestamp": 1715268993022
}

```

## Create Iceberg connection

You'll need to put your own database, region, and role-arn in here.

```bash
decodable connection create                                                        \
    --name basket-iceberg                                                          \
    --type sink                                                                    \
    --connector iceberg                                                            \
    --prop catalog-database=my_db                                                  \
    --prop catalog-table=basket                                                    \
    --prop catalog-type=glue                                                       \
    --prop format=parquet                                                          \
    --prop region=us-west-2                                                        \
    --prop role-arn=                                                               \
    --prop warehouse=s3://foo/iceberg-test/                                        \
    --stream-id $(decodable query --keep-ids --name                                \
                  $(decodable query --name kafka-basket |                          \
                    yq '.spec.stream_name') |                                      \
                  yq '.metadata.id')                                               \
    --field basketId="STRING"                                                      \
    --field customerId="STRING"                                                    \
    --field customerName="STRING"                                                  \
    --field customerAddress="STRING"                                               \
    --field storeId="STRING"                                                       \
    --field storeName="STRING"                                                     \
    --field storeLocation="STRING"                                                 \
    --field products="ARRAY<ROW( productName STRING, quantity INT, unitPrice FLOAT, category STRING )>"  \
    --field timestamp="STRING"

```

Start the connection

```bash
decodable connection activate $(decodable query --name basket-iceberg --keep-ids | yq '.metadata.id') --start-position earliest
```

Check its status

```bash
decodable query --name basket-iceberg --no-spec
```


## View the S3 bucket to check Iceberg data is there

```bash
$ aws s3 ls s3://foo/iceberg-test/foo.db/foo_basket02/
                           PRE data/
                           PRE metadata/
$ aws s3 ls s3://foo/iceberg-test/foo.db/foo_basket02/data/
2024-06-05 18:07:22      30440 00000-0-dd5fc5f4-9821-448a-8bf6-b3b0a4e3d267-00001.parquet
$ aws s3 ls s3://foo/iceberg-test/foo.db/foo_basket02/metadata/
2024-06-05 18:05:37       3021 00000-63ca0b75-1511-4d8f-b67e-97d8695a9ebe.metadata.json
2024-06-05 18:07:25       4244 00001-d14c9e6e-e9c1-4fcf-a521-c80fd5c3c2a5.metadata.json
2024-06-05 18:09:14       5308 00002-1978c64b-5031-42a9-97b7-6eac6e652a02.metadata.json
[…]
```