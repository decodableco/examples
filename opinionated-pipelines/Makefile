include .env

login:
	decodable login

stream:
	jinja -d $(SCHEMA) -o model.sh templates/model.sh.j2
	chmod +x model.sh
	./model.sh


invalid:  # create a connection to kafka
	@decodable conn create \
	--name EMPLOYEE_CONFLUENT \
	--connector confluent-cloud \
	--type source \
	--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="Employee")|.id ' ) \
	--description "COMMAND for Employee" \
	--field empid=string \
	--field SSN=string \
	--field fname=string \
	--field lname=string \
	--field address=string \
	--prop cluster.api.endpoint=https://$(BOOTSTRAP) \
	--prop cluster.id=$(CLUSTER_ID) \
	--prop topic=$(TOPIC) \
	--prop format=json \
	--prop cluster.api.key=$(CONFLUENT_KEY) \
	--prop cluster.api.secret=$(CONFLUENT_SECRET)


valid:  # create a connection to kafka
	@decodable conn create \
	--name EMPLOYEE_CONFLUENT \
	--connector confluent-cloud \
	--type source \
	--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="Employee")|.id ' ) \
	--description "COMMAND for Employee" \
	--field empid=integer \
	--field SSN=string \
	--field fname=string \
	--field lname=string \
	--field address=string \
	--prop cluster.api.endpoint=https://$(BOOTSTRAP) \
	--prop cluster.id=$(CLUSTER_ID) \
	--prop topic=$(TOPIC) \
	--prop format=json \
	--prop cluster.api.key=$(CONFLUENT_KEY) \
	--prop cluster.api.secret=$(CONFLUENT_SECRET)

activate:
	decodable connection activate  $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="EMPLOYEE_CONFLUENT")|.id ' )

preview: # preview data coming in
	decodable pl preview "select * from Employee"

kconfig:
	@rm -f config.properties
	@echo "bootstrap.servers=$(BOOTSTRAP)" >> config.properties
	@echo "ssl.endpoint.identification.algorithm=https" >> config.properties
	@echo "security.protocol=SASL_SSL" >> config.properties
	@echo "sasl.mechanism=PLAIN" >> config.properties
	@echo 'sasl.jaas.config=org.apache.kafka.common.security.plain.PlainLoginModule required username="$(CONFLUENT_KEY)" password="$(CONFLUENT_SECRET)";' >> config.properties



publish: kconfig # send data to kafka
	# make publish JSON=<<path to JSON>> 
	kcat -b $(BOOTSTRAP) -F config.properties -t $(TOPIC) -k $$RANDOM -P $(JSON)

subscribe: kconfig
	kcat -b $(BOOTSTRAP) -F config.properties -t $(TOPIC) -K :

status:
	decodable connection get $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="EMPLOYEE_CONFLUENT")|.id ' ) 

error:
	decodable connection get $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="EMPLOYEE_CONFLUENT")|.id ' ) -o json \
		| jq .last_runtime_error.message -r

deactivate:
	decodable connection deactivate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="EMPLOYEE_CONFLUENT")|.id ' )
	
restart: deactivate activate

clean:
	decodable connection deactivate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="EMPLOYEE_CONFLUENT")|.id ' )
	decodable connection delete $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="EMPLOYEE_CONFLUENT")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="Employee")|.id ' )
