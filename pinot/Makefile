
include .env

EKS_CLUSTER_NAME=hubert-superset

topic:
	confluent kafka topic --cluster $(CLUSTER_ID) create osquery-pinot
	confluent kafka topic --cluster $(CLUSTER_ID) create suspicious_processes

pinot.table.config:
	@mkdir -p tables
	@jinja \
		-D BOOTSTRAP $(BOOTSTRAP) \
		-D CONFLUENT_KEY $(CONFLUENT_KEY) \
		-D CONFLUENT_SECRET $(CONFLUENT_SECRET) \
		-D CLUSTER_ID $(CLUSTER_ID) \
		-D SCHEMA_REGISTRY $(SCHEMA_REGISTRY) \
		-D CONFLUENT_SR_KEY $(CONFLUENT_SR_KEY) \
		-D CONFLUENT_SR_SECRET $(CONFLUENT_SR_SECRET) \
		-D TOPIC $(TOPIC) \
		templates/osquery-table.json.j2 \
		> tables/osquery-table.json

pinot.table: pinot.table.config
	pinot-admin AddTable \
		-tableConfigFile tables/osquery-table.json  \
		-schemaFile schemas/osquery-schema.json \
		-controllerHost $(CONTROLLER_HOST) \
		-controllerPort $(CONTROLLER_PORT) \
		-exec

suspicious.pinot.table: pinot.table.config
	pinot-admin AddTable \
		-tableConfigFile tables/osquery-suspicious-table.json  \
		-schemaFile schemas/osquery-schema.json \
		-controllerHost $(CONTROLLER_HOST) \
		-controllerPort $(CONTROLLER_PORT) \
		-exec

suspicious_osquery_cleansed:
	@decodable stream create \
		--name suspicious_osquery_cleansed \
		--description "osquery cleansed processes" \
		--field name=string \
		--field hostIdentifier=string \
		--field calendarTime=string \
		--field unixTime=bigint \
		--field epoch=bigint \
		--field counter=int \
		--field numerics=boolean \
		--field cmdline=STRING \
		--field cwd=STRING \
		--field disk_bytes_read=BIGINT \
		--field disk_bytes_written=BIGINT \
		--field egid=INT \
		--field euid=INT \
		--field gid=INT \
		--field process_name=STRING \
		--field nice=INT \
		--field on_disk=INT \
		--field parent=BIGINT \
		--field path=STRING \
		--field pgroup=BIGINT \
		--field pid=BIGINT \
		--field resident_size=BIGINT \
		--field root=STRING \
		--field sgid=INT \
		--field start_time=BIGINT \
		--field state=INT \
		--field suid=INT \
		--field system_time=BIGINT \
		--field threads=INT \
		--field total_size=BIGINT \
		--field uid=INT \
		--field user_time=BIGINT \
		--field wired_size=INT \
		--field action=string

osquery_cleansed_sink:
	@decodable conn create \
		--name osquery_cleansed_sink \
		--connector confluent-cloud \
		--type sink \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="cleansed_processes")|.id ' ) \
		--field name=string \
		--field hostIdentifier=string \
		--field calendarTime=string \
		--field unixTime=bigint \
		--field epoch=bigint \
		--field counter=int \
		--field numerics=boolean \
		--field cmdline=STRING \
		--field cwd=STRING \
		--field disk_bytes_read=BIGINT \
		--field disk_bytes_written=BIGINT \
		--field egid=INT \
		--field euid=INT \
		--field gid=INT \
		--field process_name=STRING \
		--field nice=INT \
		--field on_disk=INT \
		--field parent=BIGINT \
		--field path=STRING \
		--field pgroup=BIGINT \
		--field pid=BIGINT \
		--field resident_size=BIGINT \
		--field root=STRING \
		--field sgid=INT \
		--field start_time=BIGINT \
		--field state=INT \
		--field suid=INT \
		--field system_time=BIGINT \
		--field threads=INT \
		--field total_size=BIGINT \
		--field uid=INT \
		--field user_time=BIGINT \
		--field wired_size=INT \
		--field action=string \
		--prop cluster.api.endpoint=https://$(BOOTSTRAP) \
		--prop cluster.id=$(CLUSTER_ID) \
		--prop topic=osquery-pinot \
		--prop format=json \
		--prop cluster.api.key=$(CONFLUENT_KEY) \
		--prop cluster.api.secret=$(CONFLUENT_SECRET)

suspicious_osquery_cleansed_sink:
	@decodable conn create \
		--name suspicious_osquery_cleansed_sink \
		--connector confluent-cloud \
		--type sink \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="suspicious_osquery_cleansed")|.id ' ) \
		--field name=string \
		--field hostIdentifier=string \
		--field calendarTime=string \
		--field unixTime=bigint \
		--field epoch=bigint \
		--field counter=int \
		--field numerics=boolean \
		--field cmdline=STRING \
		--field cwd=STRING \
		--field disk_bytes_read=BIGINT \
		--field disk_bytes_written=BIGINT \
		--field egid=INT \
		--field euid=INT \
		--field gid=INT \
		--field process_name=STRING \
		--field nice=INT \
		--field on_disk=INT \
		--field parent=BIGINT \
		--field path=STRING \
		--field pgroup=BIGINT \
		--field pid=BIGINT \
		--field resident_size=BIGINT \
		--field root=STRING \
		--field sgid=INT \
		--field start_time=BIGINT \
		--field state=INT \
		--field suid=INT \
		--field system_time=BIGINT \
		--field threads=INT \
		--field total_size=BIGINT \
		--field uid=INT \
		--field user_time=BIGINT \
		--field wired_size=INT \
		--field action=string \
		--prop cluster.api.endpoint=https://$(BOOTSTRAP) \
		--prop cluster.id=$(CLUSTER_ID) \
		--prop topic=suspicious_processes \
		--prop format=json \
		--prop cluster.api.key=$(CONFLUENT_KEY) \
		--prop cluster.api.secret=$(CONFLUENT_SECRET)

suspicious_osquery_cleansed_pipeline:
	@decodable pipeline create \
		--name suspicious_osquery_cleansed_pipeline \
  		--description "demo day osquery_cleanse" \
  		- < sql/suspicious.cleanse.sql

active:
	decodable connection activate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="osquery_cleansed_sink")|.id ' )
	decodable connection activate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="suspicious_osquery_cleansed_sink")|.id ' )
	decodable pl activate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="suspicious_osquery_cleansed_pipeline")|.id ' )

flow: sink table

drop:
	pinot-admin ChangeTableState \
		-tableName osquery \
		-state drop \
		-controllerHost $(CONTROLLER_HOST) \
		-controllerPort $(CONTROLLER_PORT) 

clean: 
	decodable conn deactivate  --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="osquery_cleansed_sink")|.id ' )
	decodable conn delete $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="osquery_cleansed_sink")|.id ' )
	
	decodable conn deactivate  --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="suspicious_osquery_cleansed_sink")|.id ' )
	decodable conn delete $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="suspicious_osquery_cleansed_sink")|.id ' )
	
	decodable pl deactivate $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="suspicious_osquery_cleansed_pipeline")|.id ' )
	decodable pl delete $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="suspicious_osquery_cleansed_pipeline")|.id ' )
