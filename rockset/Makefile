
include .env
TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)

send:
	aws2 kinesis put-record \
		--stream-name hubert \
		--data $(shell echo '{ "foo":"bar$(shell echo $$RANDOM)" }' | base64) \
		--partition-key $$RANDOM 

kinesis_raw:
	decodable stream create \
		--name kinesis_raw \
		--field ID=string \
		--field Country=string \
		--field CountryCode=string \
		--field Slug=string \
		--field NewConfirmed=integer \
		--field TotalConfirmed=integer \
        --field NewDeaths=integer \
        --field TotalDeaths=integer \
        --field NewRecovered=integer \
        --field TotalRecovered=integer \
        --field Date=string \
        --field Premium='ROW<>'

kinesis:
	decodable conn create \
		--name kinesis_source \
		--connector kinesis \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="covid_cleansed")|.id ' ) \
		--field ID=string \
		--field Country=string \
		--field CountryCode=string \
		--field Slug=string \
		--field NewConfirmed=integer \
		--field TotalConfirmed=integer \
        --field NewDeaths=integer \
        --field TotalDeaths=integer \
        --field NewRecovered=integer \
        --field TotalRecovered=integer \
        --field Date=timestamp \
		--prop aws.credentials.role.arn=$(AWS_ARN) \
		--prop aws.region=$(AWS_REGION) \
		--prop stream=$(STREAM) \
		--prop format=json
		

run:
	python mqttclient.py


consume:
	@mosquitto_sub -h $(MQTT_HOST) -t $(TOPIC) -p $(MQTT_PORT) -u $(MQTT_USER) -P $(MQTT_PASSWORD)


pub:
	@mosquitto_pub -h $(MQTT_HOST) -t owntracks/foo/bar -p $(MQTT_PORT) -u $(MQTT_USER) -P $(MQTT_PASSWORD) \
	-m '{ "_type": "location", "acc": 11, "alt": 120, "batt": $$RANDOM, "bs": 1, "BSSID": "xx:xx:x:xx:xx:xx", "cog": 147, "conn": "w", "lat": 41.702894, "lon": -74.082373, "m": 1, "p": 99.715, "SSID": "my-router", "tid": "0E", "tst": 1657136071, "vac": 11, "vel": $(RANDOM) }'

