# TLS/mTLS Configuration of Apache Kafka - SELF SIGNING FLOW
Generating a self-signed certificate for a Kafka broker.


# Generate CA
- ca-cert = public CA certificate
- ca-key = private key of the ca-cert

```mermaid
flowchart TD;
classDef make fill:blue,stroke:#333,stroke-width:4px
classDef file fill:green,stroke:#333,stroke-width:4px

    make:CA:::make-->file:ca-key:::file
    make:CA-->file:ca-cert:::file

```

# Broker Side
- cert-file = signing request
- cert-signed = signed certificate

```mermaid
flowchart TD;
classDef make fill:blue,stroke:#333,stroke-width:4px
classDef file fill:green,stroke:#333,stroke-width:4px

    make:BROKER_KEYSTORE:::make-->jks[file:kafka.server.keystore.jks]:::file-->
    make:BROKER_CERT_REQUEST:::make-->file:cert-file:::file

    file:ca-cert-->make:SELF_SIGN:::make
    file:ca-key-->make:SELF_SIGN
    file:cert-file-->make:SELF_SIGN

    make:SELF_SIGN-->file:cert-signed:::file


    file:ca-cert-->make:TRUSTSTORE:::make-->file:kafka.server.truststore.jks:::file

    jks-->make:KEYSTORE_CACERT:::make
    file:ca-cert-->make:KEYSTORE_CACERT

    file:cert-signed-->make:KEYSTORE_SIGNED_CERT
    jks-->make:KEYSTORE_SIGNED_CERT:::make
```


## server.properties file
Requires Kafka restart.

```properties
listeners=PLAINTEXT://0.0.0.0:9092,SSL://0.0.0.0:9093
advertised.listeners=PLAINTEXT://<HOSTNAME>:9092,SSL://<HOSTNAME>:9093

ssl.keystore.location=<path>/kafka.server.keystore.jks
ssl.keystore.password=yourpassword
ssl.key.password=yourpassword
ssl.truststore.location=<path>/kafka.server.truststore.jks
ssl.truststore.password=yourpassword

```

Verify the SSL port
```bash
openssl s_client -connect <HOSTNAME>:9093
```

# Client configuration

```mermaid
flowchart TD;
classDef make fill:blue,stroke:#333,stroke-width:4px
classDef file fill:green,stroke:#333,stroke-width:4px

file:ca-cert-->make:CLIENT_TRUSTORE:::make-->jks[file:kafka.client.truststore.jks]:::file

```

## properties file

```properties
security.protocol=SSL
ssl.truststore.location=<path>/kafka.client.truststore.jks
ssl.truststore.password=yourclientpassword
```

Producer
```bash
./kafka-console-producer.sh --broker-list HOSTNAME:9093 --topic mytopic --producer.config PATH_TO_THE_ABOVE_PROPERTIES

```

Consumer
```bash
./kafka-console-consumer.sh --broker-list HOSTNAME:9093 --topic mytopic --consumer.config PATH_TO_THE_ABOVE_PROPERTIES
```

# mTLS
Client authentication. Broker work is same as above.


```mermaid
flowchart TD;
classDef make fill:blue,stroke:#333,stroke-width:4px
classDef file fill:green,stroke:#333,stroke-width:4px

make:CLIENT_KEYSTORE:::make-->jks[file:kafka.client.keystore.jks]:::file

jks-->make:CLIENT_SIGN:::make-->file:client-cert-sign-request:::file

file:ca-cert-->make:CA_CLIENT_SIGN:::make-->file:client-cert-signed:::file
file:ca-key-->make:CA_CLIENT_SIGN
file:client-cert-sign-request:::file-->make:CA_CLIENT_SIGN

file:kafka.client.keystore.jks-->make:CLIENT_CA:::make
file:ca-cert-->make:CLIENT_CA:::make

file:kafka.client.keystore.jks-->make:CLIENT_SIGNED:::make
file:client-cert-signed-->make:CLIENT_SIGNED
```

## server.properties

```properties
ssl.client.auth=required
```

## client.properties

```properties
security.protocol=SSL
ssl.truststore.location=<path to your truststore>
ssl.truststore.password=<the password>
ssl.keystore.location=<path to your keystore>
ssl.keystore.password=<the password>
ssl.key.password=<the password>

```


Producer
```bash
./kafka-console-producer.sh --broker-list HOSTNAME:9093 --topic mytopic --producer.config PATH_TO_THE_ABOVE_PROPERTIES

```

Consumer
```bash
./kafka-console-consumer.sh --bootstrap-server HOSTNAME:9093 --topic mytopic --consumer.config PATH_TO_THE_ABOVE_PROPERTIES
```
