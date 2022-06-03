include .env

login:
	decodable login

async:
	decodable stream list -o json | jq -r 'select(.name | startswith("dataproduct"))' | \
		jq -r .id | \
		xargs -n1 decodable stream get -o json | \
		jq -s '{dp:.}' \
		> dp.json

	cat dp.json | jq

	jinja -d dp.json -o $(ACCOUNT).yaml templates/dp.decodable.yaml.j2

html:
	ag $(ACCOUNT).yaml @asyncapi/html-template -o output --force-write
	
s3:
	jinja -f yaml -d $(ACCOUNT).yaml -o s3-test.sh templates/s3.sh.j2


