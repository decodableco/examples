ACCOUNT=change-me

login:
	decodable login

async:
	decodable stream list -o json | \
		jq -r 'select(.name | startswith("dataproduct"))|.id' | \
		xargs -n1 decodable stream get -o json | \
		jq -s '{dp:.}' \
		> dp.json

	cat dp.json | jq

	jinja -d dp.json -o $(ACCOUNT).yaml templates/dp.decodable.yaml.j2

html:
	ag $(ACCOUNT).yaml @asyncapi/html-template -o output --force-write
	
s3:
	cat $(ACCOUNT).yaml | yq .components.schemas.dataproduct_envoy_events

	decodable conn create \
		--name s3_dataproducts \
		--connector s3 \
		--type sink \
		--stream-id 53983865 \
		--field ts=TIMESTAMP \
		--field status=INT \
		--field method=STRING \
		--field path=STRING \
		--prop bucket=decodable-hubert \
		--prop directory=dataproducts \
		--prop format=json \
		--prop region=us-west-2 \
		--prop role-arn=arn:aws:iam::671293015970:role/decodable-hubert-s3-role

	# decodable connection activate e7f371d5

