
include .env
TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)

send:
	aws2 kinesis put-record \
		--stream-name hubert \
		--data $(shell echo '{ "foo":"bar$(shell echo $$RANDOM)" }' | base64) \
		--partition-key $$RANDOM 

wiki_raw:
	@decodable stream create \
		--name wikipedia_raw \
		--field title=string \
		--field title_detail="ROW<`type` STRING, `language` STRING, `base` STRING, `value` STRING>" \
		--field links="ARRAY<ROW<`rel` STRING, `type` STRING, `href` STRING>>" \
		--field link=string \
		--field id=string \
		--field guidislink=boolean \
		--field summary=string \
		--field summary_detail="ROW<`type` STRING, `language` STRING, `base` STRING, `value` STRING>" \
		--field published=string \
		--field published_parsed="ARRAY<INT>" \
		--field authors="ARRAY<ROW<`name` STRING>>" \
		--field author=string \
		--field author_detail="ROW<`name` STRING>" \
		--field comments=string

wiki_flatten:
	@decodable stream create \
		--name wiki_flatten \
		--field id=string \
		--field title=string \
		--field link=string \
		--field guidislink=boolean \
		--field published=string \
		--field summary=string \
		--field author=string \
		--field comments=string \
		--field diff_id=string \
		--field oldid=string \
		--field url_title=string \
		--field type=string

wikipedia_kiensis:
	@decodable conn create \
		--name wikipedia_kiensis \
		--connector kinesis \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="mqtt_raw")|.id ' ) \
		--prop aws.credentials.role.arn=$(ARN) \
		--prop aws.region=$(REGION) \
		--prop stream=$(KINESIS_STREAM) \
		--prop format=json

wikipedia_confluent_sink:
	@decodable conn create \
		--name wikipedia_confluent_sink \
		--connector confluent-cloud \
		--type sink \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="wiki_flatten")|.id ' ) \
		--prop cluster.api.endpoint=https://$(BOOTSTRAP) \
		--prop cluster.id=$(CLUSTER_ID) \
		--prop topic=$(TOPIC) \
		--prop format=json \
		--prop cluster.api.key=$(CONFLUENT_KEY) \
		--prop cluster.api.secret=$(CONFLUENT_SECRET)

pipeline:
	@decodable pipeline create \
		--name wiki_confluent_sink \
  		- < pipelines/wiki_flatten.sql

flow: wiki_raw wiki_flatten wikipedia_kiensis wikipedia_confluent_sink pipeline
