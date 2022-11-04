include .env


raw:
	decodable stream create \
		--name dynamodb_cdc_customers \
		--field awsRegion=string \
		--field eventID=string \
		--field eventName=string \
		--field userIdentity=string \
		--field recordFormat=string \
		--field tableName=string \
		--field dynamodb="row< \
			ApproximateCreationDateTime bigint, \
			Keys row<userid row<N int>>, \
			NewImage row<last_name row<S string>, phone row<N int>, userid row<N int>, first_name row<S string> >, \
			OldImage row<last_name row<S string>, phone row<N int>, userid row<N int>, first_name row<S string> >, \
			SizeBytes int >" \
		--field eventSource=string

kinesis:
	decodable conn create \
		--name dynamodb_cdc_customers_source \
		--connector kinesis \
		--type source \
		--stream-id=$(shell decodable stream list -o json | jq -sr '.[] |select(.name=="dynamodb_cdc_customers")|.id ' ) \
		--prop aws.credentials.role.arn=$(AWS_ARN) \
		--prop aws.region=$(AWS_REGION) \
		--prop format=json \
		--prop stream=$(AWS_KINESIS_STREAM)


load:
	node load.js