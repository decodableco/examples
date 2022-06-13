
include .env

osq:
	sudo osqueryd --extension pulsar.ext --logger_plugin=pulsar_logger  --config_plugin=osquery_config --allow_unsafe --disable_events=false
	
	
subscribe:
	@./consumer.py $(PULSAR_ENDPOINT) $(TOPIC) $(DS_TOKEN) myconsumer	