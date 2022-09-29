
include .env

mtls:
	rm -rf ssl
	mkdir ssl

	make create.ca
	
	make create.keystore FOR=server PASSWORD=$(SRVPASS) HOST=$(BROKER_HOST)
	make create.keystore FOR=client PASSWORD=$(CLIPASS) HOST=$(shell hostname)
	
	make add.ca.to.truststore FOR=server PASSWORD=$(SRVPASS)
	make add.ca.to.truststore FOR=client PASSWORD=$(CLIPASS)

	make get.cert.from.keystore FOR=server PASSWORD=$(SRVPASS)
	make get.cert.from.keystore FOR=client PASSWORD=$(CLIPASS)

	make sign.cert FOR=server PASSWORD=$(SRVPASS)
	make sign.cert FOR=client PASSWORD=$(CLIPASS)

	make import.cacert.signed.cert.keystore FOR=server PASSWORD=$(SRVPASS)
	make import.cacert.signed.cert.keystore FOR=client PASSWORD=$(CLIPASS)

	make send.server

create.ca:
	openssl \
		req -new -x509 \
		-keyout ssl/ca-key \
		-out ssl/ca-cert \
		-days 365 \
		-subj "/CN=sample.myhost.com" \
		-nodes

create.keystore:
	keytool \
		-keystore ssl/kafka.$(FOR).keystore.jks \
		-alias $(FOR) \
		-validity 365 \
		-genkey \
		-keyalg RSA \
		-storepass ${PASSWORD} \
		-keypass ${PASSWORD} \
		-dname "CN=${HOST}" \
		-ext SAN=DNS:${HOST}

add.ca.to.truststore:
	keytool \
		-keystore ssl/kafka.$(FOR).truststore.jks \
		-alias CARoot \
		-storepass $(PASSWORD) \
		-importcert \
		-file ssl/ca-cert \
		-noprompt

get.cert.from.keystore:
	keytool \
		-keystore ssl/kafka.$(FOR).keystore.jks \
		-alias $(FOR) \
		-storepass $(PASSWORD) \
		-certreq \
		-file ssl/$(FOR)-cert-file

sign.cert:
	make sign.cert2 IN=ssl/$(FOR)-cert-file OUT=ssl/$(FOR)-cert-signed

sign.cert2:
	openssl \
		x509 -req \
		-CA ssl/ca-cert \
		-CAkey ssl/ca-key \
		-in $(IN) \
		-out $(OUT) \
		-days 365 \
		-CAcreateserial \
		-passin pass:$(PASSWORD)

import.cacert.signed.cert.keystore:
	keytool \
		-keystore ssl/kafka.$(FOR).keystore.jks \
		-alias CARoot \
		-storepass $(PASSWORD) \
		-importcert \
		-file ssl/ca-cert \
		-noprompt
	
	keytool \
		-keystore ssl/kafka.$(FOR).keystore.jks \
		-alias $(FOR) \
		-storepass $(PASSWORD) \
		-importcert \
		-file ssl/$(FOR)-cert-signed \
		-noprompt

send.server:
	scp -i ${PEMDEVW} ssl/kafka.server.*.jks ec2-user@$(BROKER_HOST):$(SSL_DIR)

ssl.verify:
	# openssl s_client -connect ${BROKER_HOST}:9093
	openssl s_client -connect 34.222.216.72:9093

delete.stream:
	-@decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="crypto_mtls")|.id ' )

create.stream:
	-@decodable stream create \
		--name crypto_mtls \
		--field id=STRING \
		--field currency=STRING \
		--field symbol=STRING \
		--field name=STRING \
		--field logo_url=STRING \
		--field status=STRING \
		--field price=STRING \
		--field price_date=STRING \
		--field price_timestamp=STRING \
		--field circulating_supply=STRING \
		--field max_supply=STRING \
		--field market_cap=STRING \
		--field market_cap_dominance=STRING \
		--field num_exchanges=STRING \
		--field num_pairs=STRING \
		--field num_pairs_unmapped=STRING \
		--field first_candle=STRING \
		--field first_trade=STRING \
		--field first_order_book=STRING \
		--field rank=STRING \
		--field rank_delta=STRING \
		--field high=STRING \
		--field high_timestamp=STRING 

deactivate.conn.mtls:
	-@decodable conn deactivate $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="kafka_mtls_source")|.id ' )

delete.conn.mtls:
	-@decodable conn delete $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="kafka_mtls_source")|.id ' )

create.conn.mtls: 
	decodable conn create \
		--name kafka_mtls_source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="crypto_mtls")|.id ' ) \
		--connector kafka \
		--type source \
		--prop format=json \
		--prop bootstrap.servers=${BROKER_HOST}:9093  \
		--prop topic=crytpo_mtls                                           \
		--prop security.protocol=TLS                                       \
		--prop tls.client.certificate.type=CSR                     \
		--prop properties.ssl.endpoint.identification.algorithm=\
		--prop tls.broker.certificate=@ssl/server-cert-signed \

create.all: mtls deactivate.conn.mtls delete.conn.mtls delete.stream create.stream create.conn.mtls add.decodable

add.decodable:
	decodable connection \
		get $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="kafka_mtls_source")|.id ' ) \
		-o json | jq -r '.properties."tls.client.certificate"' \
		> decodable_client_cert.pem

	make sign.cert2 IN=decodable_client_cert.pem OUT=decodable_client_cert_signed.pem PASSWORD=$(SRVPASS)

	decodable conn \
		update $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="kafka_mtls_source")|.id ' ) \
  		--prop tls.broker.signed_client_certificate=@decodable_client_cert_signed.pem

java.consume:
	KAFKA_OPTS="-Djavax.net.debug=ssl"
	~/development/cp/confluent-7.1.2/bin/kafka-console-consumer \
		--bootstrap-server $(BROKER_HOST):9093 \
		--from-beginning \
		--topic crytpo_mtls \
		--consumer.config client.properties

java.produce:
	KAFKA_OPTS="-Djavax.net.debug=ssl"
	~/development/cp/confluent-7.1.2/bin/kafka-console-producer \
		--bootstrap-server $(BROKER_HOST):9093 \
		--topic crytpo_mtls \
		--producer.config client.properties

create.topic:
	KAFKA_OPTS="-Djavax.net.debug=ssl"
	~/development/cp/confluent-7.1.2/bin/kafka-topics \
		--bootstrap-server $(BROKER_HOST):9093 \
		--create \
		--topic crytpo_mtls \
		--command-config client.properties

list.topics:
	KAFKA_OPTS="-Djavax.net.debug=ssl"
	~/development/cp/confluent-7.1.2/bin/kafka-topics \
		--bootstrap-server $(BROKER_HOST):9093 \
		--list \
		--command-config client.properties