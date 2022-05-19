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

publish: # send data to kafka
	# make publish JSON=<<path to JSON>> 
	kcat -b $(BOOTSTRAP) -F config.properties -t $(TOPIC) -k $$RANDOM -P $(JSON)

subscribe: 
	kcat -b $(BOOTSTRAP) -F config.properties -t $(TOPIC) -K :

status:
	decodable connection get $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="EMPLOYEE_CONFLUENT")|.id ' ) -o json \
		| jq .last_runtime_error.message -r

