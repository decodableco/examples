
include .env

TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)

raw:
	@decodable stream create \
		--name customers \
		--field id=STRING \
		--field first_name=STRING \
		--field last_name=STRING \
		--field email=STRING \
		--field gender=STRING \
		--field ip_address=STRING \
		--field cc=BIGINT

masked:
	@decodable stream create \
		--name customers_masked \
		--field id=STRING \
		--field first_name=STRING \
		--field last_name=STRING \
		--field email=STRING \
		--field gender=STRING \
		--field ip_address=STRING \
		--field cc=STRING

source:
	@decodable conn create \
		--name rest_customers \
		--connector rest \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="customers")|.id ' ) \
		--field id=STRING \
		--field first_name=STRING \
		--field last_name=STRING \
		--field email=STRING \
		--field gender=STRING \
		--field ip_address=STRING \
		--field cc=BIGINT \
		--prop format=json

run:
	$(eval ID:=$(shell decodable connection list -o json | jq -sr '.[] |select(.name=="rest_customers")|.id ' ))
	$(eval EP:=$(shell decodable connection get $(ID) -o json |jq -r '.properties.endpoint'))
	@python loader.py data/MOCK_DATA.csv $(TOKE) $(EP)


active:
	decodable conn activate $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="rest_customers")|.id ' )
	decodable pipeline activate $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="regexp_replace")|.id ' )
	decodable pipeline activate $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="overlay")|.id ' )
	

pipeline:
	@decodable pipeline create \
		--name regexp_replace \
  		- < sql/regex.sql

	@decodable pipeline create \
		--name overlay \
  		- < sql/overlay.sql

flow: raw masked source pipeline

deactive:
	decodable conn deactivate $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="rest_customers")|.id ' )
	decodable pipeline deactivate $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="regexp_replace")|.id ' )
	decodable pipeline deactivate $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="overlay")|.id ' )

clean: deactive
	decodable conn delete $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="rest_customers")|.id ' )
	decodable pipeline delete $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="regexp_replace")|.id ' )
	decodable pipeline delete $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="overlay")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="customers")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="customers_masked")|.id ' )