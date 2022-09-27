# TLS/mTLS Configuration of Apache Kafka - SELF SIGNING FLOW
Generating a self-signed certificate for a Kafka broker.


# Download Apache Kafka
curl -O https://downloads.apache.org/kafka/3.2.3/kafka_2.13-3.2.3.tgz


# Generate CA
- ca-cert = public CA certificate
- ca-key = private key of the ca-cert

```mermaid
flowchart TD;

    ca[Create CA]-->ca-key
    ca-->ca-cert

```

# Create a Keystore and Create a Cert Sign Request

```mermaid
flowchart TD;

    s[/start\]-->|Create Broker Keystore| server[kafka.server.keystore.jks]

    server==>|create a signing request| server-cert-file-request

    s-->|Create Client Keystore| client[kafka.client.keystore.jks]

    client==>|create a signing request| client-cert-file-request

    subgraph Client
    client
    client-cert-file-request
    end

    subgraph Server
    server
    server-cert-file-request
    end

```

# Sign Certificate
```mermaid
flowchart TD;

    ca-cert-->sign1[[Sign Certificate]]
    ca-key-->sign1
    server-cert-file-request-->sign1

    sign1-->signed1(server-cert-signed fa:fa-cert)



    ca-cert-->sign2[[Sign Certificate]]
    ca-key-->sign2
    client-cert-file-request-->sign2

    sign2-->signed2(client-cert-signed fa:fa-cert)

    subgraph Certificate Authority
    ca-cert
    ca-key
    end


```

# Import Certificates
```mermaid
flowchart TD;


    ca-cert-->|import|serverks[kafka.server.keystore.jks]
    ca-cert-->|import|serverts[kafka.server.truststore.jks]

    server-cert-signed-->|import|serverks

    ca-cert-->|import|clientks[kafka.client.keystore.jks]
    ca-cert-->|import|clientts[kafka.client.truststore.jks]


    cert-cert-signed-->|import|clientks

    subgraph Client
    clientks
    clientts
    end

    subgraph Server
    serverks
    serverts
    end

```

# Broker Configuration server.properties file
Requires Kafka restart.

set this environment property to show SSL debug logs.
```bash
export KAFKA_OPTS=-Djavax.net.debug=all
```

Create a `ssl` directory in KAFKA_HOME so that you can utilize the make command at the end of this README. Place all of the jks files in that directory and set the path as a relative path in the properties below. (replace `<path>` with `ssl`)

```properties
listeners=PLAINTEXT://0.0.0.0:9092,SSL://0.0.0.0:9093
advertised.listeners=PLAINTEXT://<HOSTNAME>:9092,SSL://<HOSTNAME>:9093

ssl.keystore.location=<path>/kafka.server.keystore.jks
ssl.keystore.password=yourpassword
ssl.key.password=yourpassword
ssl.truststore.location=<path>/kafka.server.truststore.jks
ssl.truststore.password=yourpassword

ssl.client.auth=required

```

## Verify the SSL port
```bash
openssl s_client -connect <HOST_IP>:9093
```


# Client Configuration properties file

## client.properties

```properties
security.protocol=SSL
ssl.truststore.location=<path to your truststore>
ssl.truststore.password=<the password>
ssl.keystore.location=<path to your keystore>
ssl.keystore.password=<the password>
ssl.key.password=<the password>

ssl.endpoint.identification.algorithm=
```


Host name verification of servers is enabled by default for client connections as well as inter-broker connections to prevent man-in-the-middle attacks. Server host name verification may be disabled by setting ssl.endpoint.identification.algorithm to an empty string. For example,

```properties
ssl.endpoint.identification.algorithm=
```

Producer
```bash
./kafka-console-producer.sh --broker-list HOSTNAME:9093 --topic mytopic --producer.config PATH_TO_THE_ABOVE_PROPERTIES

```

Consumer
```bash
./kafka-console-consumer.sh --broker-list HOSTNAME:9093 --topic mytopic --consumer.config PATH_TO_THE_ABOVE_PROPERTIES
```


# Configuring Decodable

The common name (CN) must match exactly the fully qualified domain name (FQDN) of the server. The client compares the CN with the DNS domain name to ensure that it is indeed connecting to the desired server, not a malicious one. The hostname of the server can also be specified in the Subject Alternative Name (SAN). Since the distinguished name is used as the server principal when SSL is used as the inter-broker security protocol, it is useful to have hostname as a SAN rather than the CN.

To show the CN or SAN in a signed certificate, run the command below:

```bash
openssl x509 -noout -subject -in your-signed-cert
```


# Automated

## .env file
Create a directory called `ssl` in your KAFKA_HOME directory. The make command below will copy the updated jks files to that directory. You'll need to restart Kafka.

```properties
BROKER_HOST=<THE HOST NAME TO YOUR BROKER>
SRVPASS=server_pw
CLIPASS=client_pw
SSL_DIR=<THE PATH TO YOUR SSL DIRECTORY>
```

Run the command below to create to create the certificates, the connection and streams.
```bash
$ make create.all
```