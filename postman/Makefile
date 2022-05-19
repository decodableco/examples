include .env

DECODABLE_TOKEN := $(shell yq -r .tokens.default.access_token ~/.decodable/auth)
CONNECTION := EMPLOYEE_REST_API

login:
	decodable login

stream:
	jinja -d $(SCHEMA) -o model.sh templates/model.sh.j2
	chmod +x model.sh
	./model.sh

rest:
	decodable conn create \
		--name $(CONNECTION) \
		--connector rest \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="User")|.id ' ) \
		--description "COMMAND for Employee" \
		--field id=integer \
		--field bday=date \
		--field fname=string \
		--field lname=string \
		--field address=string \
		--prop format=json 

activate:
	decodable connection activate  $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="$(CONNECTION)")|.id ' )
	decodable connection get $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="$(CONNECTION)")|.id ' )

post:  # send data
	curl --request POST \
	--url https://$(ACCOUNT).api.decodable.co/v1alpha2/connections/$(CONNECTION)/events \
	--header 'Accept: application/json' \
	--header 'Authorization: Bearer $(DECODABLE_TOKEN)' \
	--header 'Content-Type: application/json' \
	--data "@$(JSON)" | jq

list:  # send data
	curl --request GET \
	--url https://$(ACCOUNT).api.decodable.co/v1alpha2/streams \
	--header 'Accept: application/json' \
	--header 'Authorization: Bearer $(DECODABLE_TOKEN)' \
	--header 'Content-Type: application/json' \
	| jq


preview: # preview data coming in
	decodable pl preview "select * from User"

clean:
	C=$(shell decodable connection list -o json | jq -sr '.[] |select(.name=="$(CONNECTION)")|.id ' )
	echo ->$(C)<-
	decodable connection deactivate  $(C)
	decodable connection delete  $(C)
