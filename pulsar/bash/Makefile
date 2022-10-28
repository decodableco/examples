
include .env


stream:
	decodable stream create \
		--name pulsar_cdc
		--field userid="int primary key" \
		--field fist_name=string 

source:
	decodable conn \
		create --name pulsar-to-change-stream \
		--connector pulsar \
		--type source \
		--stream-id d824b7bc \
		--prop admin-url="$(ADMIN)" \
		--prop format=debezium-json \
		--prop service-url="$(URL)" \
		--prop topic=persistent://public/default/user

produce:
	pulsar-client \
		--url $(URL) \
		--auth-plugin org.apache.pulsar.client.impl.auth.oauth2.AuthenticationOAuth2 \
		--auth-params '{ \
			"privateKey":"$(KEY_FILE)", \
			"issuerUrl":"https://auth.streamnative.cloud/", \
			"audience":"$(AUDIENCE)" \
		}' \
		produce \
		-f $(shell pwd)/data.json \
		"persistent://public/default/user"
