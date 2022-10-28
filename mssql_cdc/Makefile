include .env
TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)

sqlserver:
	# username= sa
	docker run \
		-e "ACCEPT_EULA=Y" \
		-e "MSSQL_AGENT_ENABLED=True" \
		-e "MSSQL_SA_PASSWORD=yourStrong(!)Password" \
		-e "MSSQL_PID=Standard" \
		-p 1433:1433 \
		mcr.microsoft.com/mssql/server:2019-latest 

raw:
	decodable stream create \
		--name mssql_cdc \
		--field payload="ROW< \
			before ROW<userid int, first_name string, last_name string, phone string>, \
			after ROW<userid int, first_name string, last_name string, phone string>, \
			op string, ts_ms bigint>"

materialized:
	decodable stream create \
		--name mssql_cdc_materialized \
		--field userid="INT NOT NULL PRIMARY KEY" \
		--field first_name=STRING \
		--field last_name=STRING \
		--field phone=STRING \
		--field op=STRING \
		--field ts_ms="timestamp(3)" \
		--watermark "\`ts_ms\` as \`ts_ms\` - interval '30' second"

partitioned:
	decodable stream create \
		--name mssql_cdc_partitioned \
		--field userid=INT \
		--field first_name=STRING \
		--field last_name=STRING \
		--field phone=STRING \
		--field op=STRING \
		--field ts_ms="timestamp(3)" \
		--partition-key userid \
		--watermark "\`ts_ms\` as \`ts_ms\` - interval '30' second"

kinesis:
	decodable conn create \
		--name mssql-cdc \
		--connector kinesis \
		--type source \
		--stream-id=$(shell decodable stream list -o json | jq -sr '.[] |select(.name=="mssql_cdc")|.id ' ) \
		--prop aws.credentials.role.arn=$(ARN) \
		--prop aws.region=$(REGION) \
		--prop format=json \
		--prop stream=$(STREAM)

flow: raw kinesis
