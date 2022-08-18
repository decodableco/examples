
include .env

# crypto_raw_array:
# 	decodable stream create \
# 		--name crypto_raw_array \
# 		--field records="ARRAY<ROW<id STRING, currency STRING, symbol STRING, name STRING, logo_url STRING, status STRING, price double, price_date STRING, price_timestamp STRING, circulating_supply STRING, max_supply STRING, market_cap STRING, market_cap_dominance STRING, >>"

crypto_desc:
	decodable stream create \
		--name crypto_desc \
		--field id="STRING primary key" \
		--field currency=STRING

crypto_raw:
	decodable stream create \
		--name crypto_raw \
		--field id=STRING \
		--field currency=STRING \
		--field symbol=STRING \
		--field name=STRING \
		--field logo_url=STRING \
		--field status=STRING \
		--field price=STRING \
		--field price_date=STRING \
		--field price_timestamp=STRING \
		--field circulating_supply=STRING \
		--field max_supply=STRING \
		--field market_cap=STRING \
		--field market_cap_dominance=STRING \
		--field num_exchanges=STRING \
		--field num_pairs=STRING \
		--field num_pairs_unmapped=STRING \
		--field first_candle=STRING \
		--field first_trade=STRING \
		--field first_order_book=STRING \
		--field rank=STRING \
		--field rank_delta=STRING \
		--field high=STRING \
		--field high_timestamp=STRING \
		--field 1d="ROW<volume STRING, price_change STRING, price_change_pct STRING, volume_change STRING, volume_change_pct STRING, market_cap_change STRING, market_cap_change_pct STRING >" \
		--field 30d="ROW<volume STRING, price_change STRING, price_change_pct STRING, volume_change STRING, volume_change_pct STRING, market_cap_change STRING, market_cap_change_pct STRING >"

crypto_raw_table:
	decodable stream create \
		--name crypto_raw_table \
		--field id="STRING primary key" \
		--field currency=STRING \
		--field symbol=STRING \
		--field name=STRING \
		--field logo_url=STRING \
		--field status=STRING \
		--field price=STRING \
		--field price_date=STRING \
		--field price_timestamp=STRING \
		--field circulating_supply=STRING \
		--field max_supply=STRING \
		--field market_cap=STRING \
		--field market_cap_dominance=STRING \
		--field num_exchanges=STRING \
		--field num_pairs=STRING \
		--field num_pairs_unmapped=STRING \
		--field first_candle=STRING \
		--field first_trade=STRING \
		--field first_order_book=STRING \
		--field rank=STRING \
		--field rank_delta=STRING \
		--field high=STRING \
		--field high_timestamp=STRING \
		--field 1d="ROW<volume STRING, price_change STRING, price_change_pct STRING, volume_change STRING, volume_change_pct STRING, market_cap_change STRING, market_cap_change_pct STRING >" \
		--field 30d="ROW<volume STRING, price_change STRING, price_change_pct STRING, volume_change STRING, volume_change_pct STRING, market_cap_change STRING, market_cap_change_pct STRING >"


run:
	python crypto.py

copy.stream:
	decodable stream get $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="${STREAM}")|.id ' ) -o json | \
		jq '.schema' | \


postgres_cdc:
	decodable conn create \
		--name crypto_desc_postgres_cdc \
		--connector postgres-cdc \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="crypto_desc")|.id ' ) \
		--prop database-name=postgres \
		--prop decoding.plugin.name=pgoutput \
		--prop hostname=hubert-demo.culgt83fyssj.us-west-2.rds.amazonaws.com \
		--prop port=5432 \
		--prop schema-name=public \
		--prop table-name=crypto_desc \
		--prop username=postgres \
		--prop password=$(PG_PASSWORD)

db:
	psql -h $(PG_HOST) -d $(PG_DB) -U $(PG_USER) -W 

list.topics:
	kcat -L -b $(BOOTSTRAP)

publish: kconfig # send data to kafka
	# make publish JSON=<<path to JSON>> 
	kcat -b $(BOOTSTRAP) -F config.properties -t $(TOPIC) -k $$RANDOM -P $(JSON)

subscribe: kconfig
	kcat -b $(BOOTSTRAP) -F config.properties -t $(TOPIC) -K :

