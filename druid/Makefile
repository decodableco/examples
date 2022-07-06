
include .env
TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)

view:
	@decodable stream create \
		--name covid \
		--field ID='string primary key' \
		--field Country=string \
		--field CountryCode=string \
		--field Slug=string \
		--field NewConfirmed=integer \
		--field TotalConfirmed=integer \
        --field NewDeaths=integer \
        --field TotalDeaths=integer \
        --field NewRecovered=integer \
        --field TotalRecovered=integer \
        --field Date=timestamp

cleansed:
	@decodable stream create \
		--name covid_cleansed \
		--field ID=string \
		--field Country=string \
		--field CountryCode=string \
		--field Slug=string \
		--field NewConfirmed=integer \
		--field TotalConfirmed=integer \
        --field NewDeaths=integer \
        --field TotalDeaths=integer \
        --field NewRecovered=integer \
        --field TotalRecovered=integer \
        --field Date=timestamp

raw:
	@decodable stream create \
		--name covid_raw \
		--field ID=string \
		--field Country=string \
		--field CountryCode=string \
		--field Slug=string \
		--field NewConfirmed=integer \
		--field TotalConfirmed=integer \
        --field NewDeaths=integer \
        --field TotalDeaths=integer \
        --field NewRecovered=integer \
        --field TotalRecovered=integer \
        --field Date=string \
        --field Premium='ROW<>'

source:
	@decodable conn create \
		--name covid_rest \
		--connector rest \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="covid_raw")|.id ' ) \
		--field ID=string \
		--field Country=string \
		--field CountryCode=string \
		--field Slug=string \
		--field NewConfirmed=integer \
		--field TotalConfirmed=integer \
        --field NewDeaths=integer \
        --field TotalDeaths=integer \
        --field NewRecovered=integer \
        --field TotalRecovered=integer \
        --field Date=string \
        --field Premium="ROW<>"

sink:
	@decodable connection create \
		--name covid_sink \
		--connector confluent-cloud \
		--type sink \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="covid_cleansed")|.id ' ) \
		--field ID=string \
		--field Country=string \
		--field CountryCode=string \
		--field Slug=string \
		--field NewConfirmed=integer \
		--field TotalConfirmed=integer \
        --field NewDeaths=integer \
        --field TotalDeaths=integer \
        --field NewRecovered=integer \
        --field TotalRecovered=integer \
        --field Date=timestamp \
		--prop cluster.api.endpoint=https://$(BOOTSTRAP) \
		--prop cluster.id=$(CLUSTER_ID) \
		--prop topic=$(TOPIC) \
		--prop format=json \
		--prop cluster.api.key=$(CONFLUENT_KEY) \
		--prop cluster.api.secret=$(CONFLUENT_SECRET)

pipeline:
	@decodable pipeline create \
		--name covid_cleanse \
  		- < sql/covid_cleanse.sql

flow: view raw source sink pipeline

run:
	python app/covid.py ${TOKE} ${ACCOUNT} $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="covid_rest")|.properties.endpoint ' )

