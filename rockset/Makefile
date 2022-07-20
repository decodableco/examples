
include .env
TOKE=$(shell yq -r .tokens.default.access_token ~/.decodable/auth)

send:
	aws2 kinesis put-record \
		--stream-name hubert \
		--data $(shell echo '{ "foo":"bar$(shell echo $$RANDOM)" }' | base64) \
		--partition-key $$RANDOM 

mqtt_raw:
	@decodable stream create \
		--name mqtt_raw \
		--field topic=string \
		--field payload=string \
		--field qos=integer \
		--field retain=integer

mqtt_enriched:
	@decodable stream create \
		--name mqtt_enriched \
		--field topic=STRING \
		--field user=VARCHAR \
		--field accuracy=INT \
		--field altitude=INT \
		--field battery_level=INT \
		--field battery_status=INT \
		--field BSSID=VARCHAR \
		--field course_over_ground=DOUBLE \
		--field conn=VARCHAR \
		--field lat=DOUBLE \
		--field lon=DOUBLE \
		--field barometric_pressure=DOUBLE \
		--field SSID=VARCHAR \
		--field trigger=VARCHAR \
		--field tracker_id=VARCHAR \
		--field timestamp=TIMESTAMP  \
		--field vertical_accuracy=INT \
		--field velocity=INT \
		--field status=VARCHAR NOT NULL \
		--field euclidean_distance=DOUBLE \
		--field is_home=BOOLEAN NOT NULL

k1:
	@decodable conn create \
		--name kinesis_source \
		--connector kinesis \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="mqtt_raw")|.id ' ) \
		--field topic=string \
		--field payload=string \
		--field qos=integer \
		--field retain=integer \
		--prop aws.credentials.role.arn=$(ARN) \
		--prop aws.region=$(REGION) \
		--prop stream=$(KINESIS_STREAM) \
		--prop format=json

k2:
	@decodable conn create \
		--name kinesis_sink \
		--connector kinesis \
		--type sink \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="mqtt_enriched")|.id ' ) \
		--field topic=STRING \
		--field user=VARCHAR \
		--field accuracy=INT \
		--field altitude=INT \
		--field battery_level=INT \
		--field battery_status=INT \
		--field BSSID=VARCHAR \
		--field course_over_ground=DOUBLE \
		--field conn=VARCHAR \
		--field lat=DOUBLE \
		--field lon=DOUBLE \
		--field barometric_pressure=DOUBLE \
		--field SSID=VARCHAR \
		--field trigger=VARCHAR \
		--field tracker_id=VARCHAR \
		--field timestamp=TIMESTAMP  \
		--field vertical_accuracy=INT \
		--field velocity=INT \
		--field status=VARCHAR NOT NULL \
		--field euclidean_distance=DOUBLE \
		--field is_home=BOOLEAN NOT NULL \
		--prop aws.credentials.role.arn=$(ARN) \
		--prop aws.region=$(REGION) \
		--prop stream=$(KINESIS_STREAM) \
		--prop format=json

pipeline:
	@decodable pipeline create \
		--name covid_cleanse \
  		- < sql/mqtt-location-enriched.sql

flow: mqtt_raw mqtt_enriched k1 k2 pipeline

run:
	python mqttclient.py


consume:
	@mosquitto_sub -h $(MQTT_HOST) -t $(TOPIC) -p $(MQTT_PORT) -u $(MQTT_USER) -P $(MQTT_PASSWORD)


pub:
	@mosquitto_pub -h $(MQTT_HOST) -t owntracks/foo/bar -p $(MQTT_PORT) -u $(MQTT_USER) -P $(MQTT_PASSWORD) \
	-m '{ "_type": "location", "acc": 11, "alt": 120, "batt": $$RANDOM, "bs": 1, "BSSID": "xx:xx:x:xx:xx:xx", "cog": 147, "conn": "w", "lat": 41.702894, "lon": -74.082373, "m": 1, "p": 99.715, "SSID": "my-router", "tid": "0E", "tst": 1657136071, "vac": 11, "vel": $(RANDOM) }'

