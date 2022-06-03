include .env

login:
	decodable login

raw:
	@decodable stream create \
		--name demo_day_xml_raw \
		--description "demo day xml" \
		--field xml=string

parsed:
	@decodable stream create \
		--name demo_day_parsed \
		--description "demo day parsed" \
		--field first_name=string \
		--field last_name=string 

source:  # create a connection to kafka
	@decodable conn create \
		--name XML_SOURCE \
		--connector confluent-cloud \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="demo_day_xml_raw")|.id ' ) \
		--field xml=string \
		--prop cluster.api.endpoint=https://$(BOOTSTRAP) \
		--prop cluster.id=$(CLUSTER_ID) \
		--prop topic=$(XML) \
		--prop format=raw \
		--prop cluster.api.key=$(CONFLUENT_KEY) \
		--prop cluster.api.secret=$(CONFLUENT_SECRET)

sink:  # create a connection to kafka
	@decodable conn create \
		--name XML_SINK \
		--connector confluent-cloud \
		--type sink \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="demo_day_parsed")|.id ' ) \
		--field first_name=string \
		--field last_name=string \
		--prop cluster.api.endpoint=https://$(BOOTSTRAP) \
		--prop cluster.id=$(CLUSTER_ID) \
		--prop topic=$(JSON) \
		--prop format=json \
		--prop cluster.api.key=$(CONFLUENT_KEY) \
		--prop cluster.api.secret=$(CONFLUENT_SECRET)

flow: raw parsed source sink
	@decodable pipeline create \
		--name demo_day_pipeline \
  		--description "demo day pipeline" \
  		- < sql/pipeline.sql

kconfig:
	@rm -f config.properties
	@echo "bootstrap.servers=$(BOOTSTRAP)" >> config.properties
	@echo "ssl.endpoint.identification.algorithm=https" >> config.properties
	@echo "security.protocol=SASL_SSL" >> config.properties
	@echo "sasl.mechanism=PLAIN" >> config.properties
	@echo 'sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="$(CONFLUENT_KEY)" password="$(CONFLUENT_SECRET)";' >> config.properties

publish: kconfig # send data to kafka
	# make publish DOC=<<path to XML>> 
	kcat -b $(BOOTSTRAP) -F config.properties -t $(XML) -k $$RANDOM -P $(DOC)

subscribe: kconfig
	kcat -b $(BOOTSTRAP) -F config.properties -t $(JSON) -K :

active:
	decodable connection activate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="XML_SOURCE")|.id ' )
	decodable connection activate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="XML_SINK")|.id ' )
	decodable pl activate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="demo_day_pipeline")|.id ' )
	
deactive:
	decodable connection deactivate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="XML_SOURCE")|.id ' )
	decodable connection deactivate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="XML_SINK")|.id ' )
	decodable pl deactivate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="demo_day_pipeline")|.id ' )
	
restart: deactive active

clean: deactive
	decodable connection delete  $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="XML_SOURCE")|.id ' )
	decodable connection delete $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="XML_SINK")|.id ' )
	decodable pl delete $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="demo_day_pipeline")|.id ' )
	decodable stream delete  $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="demo_day_xml_raw")|.id ' )
	decodable stream delete  $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="demo_day_parsed")|.id ' )
	