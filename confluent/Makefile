include .env
SHELL := bash

P='current_'
streams = clickstream_users users clickstream_change clickstream_append

init:
	confluent login --organization-id ${ORG}

# STREAMS
clickstream.append:
	-decodable stream create \
		--name clickstream_append \
		--field ip="STRING NOT NULL" \
		--field userid="INT NOT NULL" \
		--field remote_user="STRING NOT NULL" \
		--field time="STRING NOT NULL" \
		--field _time="BIGINT NOT NULL" \
		--field request="STRING NOT NULL" \
		--field status="STRING NOT NULL" \
		--field bytes="STRING NOT NULL" \
		--field referrer="STRING NOT NULL" \
		--field agent="STRING NOT NULL"

clickstream.change:
	-decodable stream create \
		--name clickstream_change \
		--field id="STRING primary key" \
		--field ip="STRING" \
		--field userid="INT NOT NULL" \
		--field remote_user="STRING NOT NULL" \
		--field time=string \
		--field _time=timestamp \
		--field request="STRING NOT NULL" \
		--field status="STRING NOT NULL" \
		--field bytes="STRING NOT NULL" \
		--field referrer="STRING NOT NULL" \
		--field agent="STRING NOT NULL"

clickstream_users_last_click:
	-decodable stream create \
		--name clickstream_users_last_click \
		--field userid="int primary key" \
		--field first_name=varchar \
		--field last_name=varchar \
		--field phone=varchar \
		--field ip="STRING NOT NULL" \
		--field remote_user="STRING NOT NULL" \
		--field time="STRING NOT NULL" \
		--field _time=timestamp \
		--field request="STRING NOT NULL" \
		--field status="STRING NOT NULL" \
		--field bytes="STRING NOT NULL" \
		--field referrer="STRING NOT NULL" \
		--field agent="STRING NOT NULL"

users.change:
	-decodable stream create \
		--name users \
		--field userid="int primary key" \
		--field first_name=varchar \
		--field last_name=varchar \
		--field phone=varchar
create.streams: users.change clickstream_users_last_click clickstream.append clickstream.change
delete.streams:
	$(eval ids := $(foreach s, $(streams), $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="$s")|.id ' )))
	$(foreach i, $(ids), decodable stream delete $i;)


# CONNECTIONS
clickstream_source:
	decodable connection create \
		--name clickstream_source \
		--connector confluent-cloud \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="clickstream_append")|.id ' ) \
		--prop cluster.api.endpoint="$(API_ENDPOINT)" \
		--prop cluster.id=$(CLUSTER_ID) \
		--prop topic=$(TOPIC) \
		--prop format=avro \
		--prop cluster.api.key=$(CONFLUENT_KEY) \
		--prop cluster.api.secret=$(CONFLUENT_SECRET)\
		--prop confluent-registry.url=$(SR_ENDPOINT) \
		--prop confluent-registry.api-key=$(SR_KEY) \
		--prop confluent-registry.api-secret=$(SR_SECRET) \

users_source:
	decodable conn create \
		--name users_source \
		--connector postgres-cdc \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="users")|.id ' ) \
		--prop database-name=${DATABASE} \
		--prop decoding.plugin.name=pgoutput \
		--prop hostname=${PGHOST} \
		--prop port=5432 \
		--prop schema-name=${SCHEMA} \
		--prop table-name=users \
		--prop username=${USER} \
		--prop password=$(PASSWORD)

clickstream_user_sink:
	decodable conn create \
		--name clickstream_user_sink \
		--connector postgres \
		--type sink \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="clickstream_users_last_click")|.id ' ) \
		--prop port=5432 \
		--prop hostname=${PGHOST} \
		--prop database-name=postgres \
		--prop schema-name=public \
		--prop password=$(PASSWORD) \
		--prop table-name=clickstream_users_last_click \
		--prop username=$(USER)
create.conns: clickstream_source clickstream_user_sink users_source 
activate.conns:
	$(eval conn := clickstream_source clickstream_user_sink users_source)
	$(eval ids := $(foreach c, $(conn), $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="$c")|.id ' )))
	$(foreach i, $(ids), decodable conn activate $i;)
deactivate.conns:
	$(eval conn := clickstream_source clickstream_user_sink users_source)
	$(eval ids := $(foreach c, $(conn), $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="$c")|.id ' )))
	$(foreach i, $(ids), decodable conn deactivate $i;)
delete.conns: deactivate.conns
	$(eval conn := clickstream_source clickstream_user_sink users_source)
	$(eval ids := $(foreach c, $(conn), $(shell decodable conn list -o json | jq -sr '.[] |select(.name=="$c")|.id ' )))
	$(foreach i, $(ids), decodable conn delete $i;)


# PIPELINES
clickstream_change_pl:
	@decodable pipeline create \
		--name clickstream_change_pl \
  		- < clickstream_change.sql

clickstream_users_last_click_pl:
	@decodable pipeline create \
		--name clickstream_users_last_click_pl \
  		- < clickstream_users_last_click.sql

create.pls: clickstream_change_pl clickstream_users_last_click_pl
activate.pls:
	$(eval pls := clickstream_change_pl clickstream_users_last_click_pl )
	$(eval ids := $(foreach i, $(pls), $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="$i")|.id ' )))
	$(foreach i, $(ids), decodable pl activate $i;)
deactivate.pls:
	$(eval pls := clickstream_change_pl clickstream_users_last_click_pl )
	$(eval ids := $(foreach i, $(pls), $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="$i")|.id ' )))
	$(foreach i, $(ids), decodable pl deactivate $i;)
delete.pls:
	$(eval pls := clickstream_change_pl clickstream_users_last_click_pl )
	$(eval ids := $(foreach i, $(pls), $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="$i")|.id ' )))
	$(foreach i, $(ids), decodable pl delete $i;)


create.all: create.streas create.pls create.conns
deactivate.all: deactivate.conns deactivate.pls
activate.all: activate.conns activate.pls
destroy.all: delete.conns delete.pls delete.streams





