
include .env

TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)

stream:
	@decodable stream create \
		--name customers \
		--field id=STRING \
		--field first_name=STRING \
		--field last_name=STRING \
		--field email=STRING \
		--field gender=STRING \
		--field ip_address=STRING \
		--field cc=BIGINT

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
	python loader.py data/MOCK_DATA.csv $(TOKE) $(EP)
