include .env

TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)

db:
	psql -h $(HOST) -d $(DB) -U $(USER) -W -f sql/postgres.sql


envoy:
	@decodable stream create \
		--name demo_day_envoy_raw \
		--description "demo day envoy" \
		--field value=string

change_stream:
	@curl --request POST \
     --url https://$(ACCOUNT).api.decodable.co/v1alpha2/streams \
     --header 'Accept: application/json' \
     --header 'Authorization: Bearer $(TOKE)' \
     --header 'Content-Type: application/json' \
	 --data '@json/change_stream.json' | \
	 jq
		
datagen:
	@decodable conn create \
		--name demo_day_datagen \
		--connector datagen \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="demo_day_envoy_raw")|.id ' ) \
		--field value=string \
		--prop format=json \
		--prop data.type=envoy \
		--prop delay=100 

# postgres:
# 	@decodable connection create \
# 		--name demo_day_postgres \
# 		--description "demo day postgres" \
# 		--connector postgres \
# 		--type sink \
# 		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="demo_day_change_stream")|.id ' ) \
# 		--field status_code="string primary key" \
# 		--field count=bigint \
# 		--prop url="jdbc:postgresql://$(HOST):5432/postgres?currentSchema=public" \
# 		--prop table-name=status_code_counts \
# 		--prop username=$(USER) \
# 		--prop password="$(PWD)"


pipeline: envoy change_stream datagen postgres
	@decodable pipeline create \
		--name demo_day_pipeline \
  		--description "demo day pipeline" \
  		- < sql/pipeline.sql


active:
	decodable conn activate $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="demo_day_datagen")|.id ' )
	decodable conn activate $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="demo_day_postgres")|.id ' )
	decodable pipeline activate $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="demo_day_pipeline")|.id ' )

deactive:
	decodable conn deactivate $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="demo_day_datagen")|.id ' )
	decodable conn deactivate $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="demo_day_postgres")|.id ' )
	decodable pipeline deactivate $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="demo_day_pipeline")|.id ' )


clean: deactive
	decodable conn delete $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="demo_day_datagen")|.id ' )
	decodable conn delete $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="demo_day_postgres")|.id ' )
	decodable pipeline delete $(shell decodable pipeline list -o json | jq -sr '.[] |select(.name=="demo_day_pipeline")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="demo_day_envoy_raw")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="demo_day_change_stream")|.id ' )
	
	
