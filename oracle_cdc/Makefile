
include .env

TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)


db:
	sqlplus $(ORA_USER)/$(ORA_PASSWORD)@$(ORA_HOST):1521/$(ORA_DATABASE)
	# sqlplus decoder/PASSWORD@hubert-oracle-cdc.cigyqvb10ueu.us-west-2.rds.amazonaws.com:1521/TEST

script:
	echo exit | sqlplus $(ORA_USER)/$(ORA_PASSWORD)@$(ORA_HOST):1521/$(ORA_DATABASE) @commands.sql

ds.download:
	curl -O https://repo1.maven.org/maven2/io/debezium/debezium-server-dist/2.0.0.Final/debezium-server-dist-2.0.0.Final.tar.gz
	tar xvf debezium-server-dist-2.0.0.Final.tar.gz
	cd debezium-server/; touch history
	cd debezium-server/; mkdir -p data/
	cd debezium-server/; touch data/offsets.dat

ds.start: 
	cd debezium-server/conf; echo 'debezium.sink.type=kinesis\n \
debezium.sink.kinesis.region=$(AWS_REGION)\n \
debezium.sink.kinesis.null.key=null\n \
\n \
debezium.source.schema.history.internal=io.debezium.storage.file.history.FileSchemaHistory\n \
debezium.source.schema.history.internal.file.filename=history\n \
debezium.source.connector.class=io.debezium.connector.oracle.OracleConnector\n \
debezium.source.offset.storage.file.filename=data/offsets.dat\n \
debezium.source.offset.flush.interval.ms=0\n \
debezium.source.database.server.name=ora_rds \
debezium.source.database.database.url=jdbc:oracle:thin:@//hubert-oracle.cigyqvb10ueu.us-west-2.rds.amazonaws.com:1521/TEST\n \
debezium.source.database.port=1521\n \
debezium.source.database.user=$(ORA_USER)\n \
debezium.source.database.hostname=$(ORA_HOST)\n \
debezium.source.database.password=$(ORA_PASSWORD)\n \
debezium.source.database.names=$(ORA_DATABASE)\n \
debezium.source.database.dbname=$(ORA_DATABASE)\n \
debezium.source.topic.prefix=ora\n \
debezium.source.database.encrypt=false\n \
debezium.source.table.include.list=admin.customers\n \
debezium.source.key.converter=org.apache.kafka.connect.json.JsonConverter\n \
debezium.source.value.converter=org.apache.kafka.connect.json.JsonConverter\n \
quarkus.http.port=6060\n \
debezium.source.key.converter.schemas.enable=false' > application.properties

	echo > debezium-server/history
	echo > debezium-server/data/offsets.dat
	cp ojdbc11.jar debezium-server/lib
	cd debezium-server/; ./run.sh | jq

ds: ds.download ds.start

raw:
	decodable stream create \
		--name oracle_cdc_kinesis_append \
		--field payload="ROW< \
			before ROW<USERID bytes, FIRST_NAME string, LAST_NAME string, PHONE string>, \
			after ROW<USERID bytes, FIRST_NAME string, LAST_NAME string, PHONE string>, \
			op string, ts_ms bigint>"

kinesis:
	decodable conn create \
		--name oracle_cdc_kinesis_append_source \
		--connector kinesis \
		--type source \
		--stream-id=$(shell decodable stream list -o json | jq -sr '.[] |select(.name=="oracle_cdc_kinesis_append")|.id ' ) \
		--prop aws.credentials.role.arn=$(AWS_ARN) \
		--prop aws.region=$(AWS_REGION) \
		--prop format=json \
		--prop stream=$(AWS_KINESIS_STREAM)

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
	
	
