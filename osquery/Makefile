
include .env

osq:
	sudo osqueryd --extension pulsar.ext --logger_plugin=pulsar_logger  --config_plugin=osquery_config --allow_unsafe --disable_events=false
	
	
subscribe:
	@./consumer.py $(PULSAR_ENDPOINT) $(SINK) $(DS_TOKEN) myconsumer	


osquery_processes:
	@decodable stream create \
		--name osquery_processes \
		--description "osquery process logs" \
		--field name=string \
		--field hostIdentifier=string \
		--field calendarTime=string \
		--field unixTime=string \
		--field epoch=string \
		--field counter=string \
		--field numerics=string \
		--field columns="ROW<``cmdline`` STRING, ``cwd`` STRING, ``disk_bytes_read`` STRING, ``disk_bytes_written`` STRING, ``egid`` STRING, ``euid`` STRING, ``gid`` STRING, ``name`` STRING, ``nice`` STRING, ``on_disk`` STRING, ``parent`` STRING, ``path`` STRING, ``pgroup`` STRING, ``pid`` STRING, ``resident_size`` STRING, ``root`` STRING, ``sgid`` STRING, ``start_time`` STRING, ``state`` STRING, ``suid`` STRING, ``system_time`` STRING, ``threads`` STRING, ``total_size`` STRING, ``uid`` STRING, ``user_time`` STRING, ``wired_size`` STRING>" \
		--field action=string

filtered_processes:
	@decodable stream create \
		--name filtered_processes \
		--description "osquery filtered processes" \
		--field name=string \
		--field hostIdentifier=string \
		--field calendarTime=string \
		--field unixTime=string \
		--field epoch=string \
		--field counter=string \
		--field numerics=string \
		--field columns="ROW<``cmdline`` STRING, ``cwd`` STRING, ``disk_bytes_read`` STRING, ``disk_bytes_written`` STRING, ``egid`` STRING, ``euid`` STRING, ``gid`` STRING, ``name`` STRING, ``nice`` STRING, ``on_disk`` STRING, ``parent`` STRING, ``path`` STRING, ``pgroup`` STRING, ``pid`` STRING, ``resident_size`` STRING, ``root`` STRING, ``sgid`` STRING, ``start_time`` STRING, ``state`` STRING, ``suid`` STRING, ``system_time`` STRING, ``threads`` STRING, ``total_size`` STRING, ``uid`` STRING, ``user_time`` STRING, ``wired_size`` STRING>" \
		--field action=string

suspicious_processes:
	@decodable stream create \
		--name suspicious_processes \
		--description "osquery suspicious processes" \
		--field name=string \
		--field hostIdentifier=string \
		--field calendarTime=string \
		--field unixTime=string \
		--field epoch=string \
		--field counter=string \
		--field numerics=string \
		--field columns="ROW<``cmdline`` STRING, ``cwd`` STRING, ``disk_bytes_read`` STRING, ``disk_bytes_written`` STRING, ``egid`` STRING, ``euid`` STRING, ``gid`` STRING, ``name`` STRING, ``nice`` STRING, ``on_disk`` STRING, ``parent`` STRING, ``path`` STRING, ``pgroup`` STRING, ``pid`` STRING, ``resident_size`` STRING, ``root`` STRING, ``sgid`` STRING, ``start_time`` STRING, ``state`` STRING, ``suid`` STRING, ``system_time`` STRING, ``threads`` STRING, ``total_size`` STRING, ``uid`` STRING, ``user_time`` STRING, ``wired_size`` STRING>" \
		--field action=string

cleansed_processes:
	@decodable stream create \
		--name cleansed_processes \
		--description "osquery cleansed processes" \
		--field name=string \
		--field hostIdentifier=string \
		--field calendarTime=string \
		--field unixTime=bigint \
		--field epoch=bigint \
		--field counter=int \
		--field numerics=boolean \
		--field cmdline=STRING \
		--field cwd=STRING \
		--field disk_bytes_read=BIGINT \
		--field disk_bytes_written=BIGINT \
		--field egid=INT \
		--field euid=INT \
		--field gid=INT \
		--field process_name=STRING \
		--field nice=INT \
		--field on_disk=INT \
		--field parent=BIGINT \
		--field path=STRING \
		--field pgroup=BIGINT \
		--field pid=BIGINT \
		--field resident_size=BIGINT \
		--field root=STRING \
		--field sgid=INT \
		--field start_time=BIGINT \
		--field state=INT \
		--field suid=INT \
		--field system_time=BIGINT \
		--field threads=INT \
		--field total_size=BIGINT \
		--field uid=INT \
		--field user_time=BIGINT \
		--field wired_size=INT \
		--field action=string

source:  # create a connection to kafka
	@decodable conn create \
		--name osquery_processes_pulsar \
		--connector pulsar \
		--type source \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="osquery_processes")|.id ' ) \
		--field name=STRING \
		--field hostIdentifier=STRING \
		--field calendarTime=STRING \
		--field unixTime=STRING \
		--field epoch=STRING \
		--field counter=STRING \
		--field numerics=STRING \
		--field columns="ROW<``cmdline`` STRING, ``cwd`` STRING, ``disk_bytes_read`` STRING, ``disk_bytes_written`` STRING, ``egid`` STRING, ``euid`` STRING, ``gid`` STRING, ``name`` STRING, ``nice`` STRING, ``on_disk`` STRING, ``parent`` STRING, ``path`` STRING, ``pgroup`` STRING, ``pid`` STRING, ``resident_size`` STRING, ``root`` STRING, ``sgid`` STRING, ``start_time`` STRING, ``state`` STRING, ``suid`` STRING, ``system_time`` STRING, ``threads`` STRING, ``total_size`` STRING, ``uid`` STRING, ``user_time`` STRING, ``wired_size`` STRING>" \
		--field action=STRING \
		--prop service-url=$(PULSAR_ENDPOINT) \
		--prop admin-url=$(ADMIN_URL) \
		--prop topic=$(SOURCE) \
		--prop token=$(DS_TOKEN) \
		--prop format=json

sink:  # persistent://decodable-hubert/default/cleansed-processes
	@decodable conn create \
		--name osquery_processes_pulsar_sink \
		--connector pulsar \
		--type sink \
		--stream-id $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="cleansed_processes")|.id ' ) \
		--field name=string \
		--field hostIdentifier=string \
		--field calendarTime=string \
		--field unixTime=bigint \
		--field epoch=bigint \
		--field counter=int \
		--field numerics=boolean \
		--field cmdline=STRING \
		--field cwd=STRING \
		--field disk_bytes_read=BIGINT \
		--field disk_bytes_written=BIGINT \
		--field egid=INT \
		--field euid=INT \
		--field gid=INT \
		--field process_name=STRING \
		--field nice=INT \
		--field on_disk=INT \
		--field parent=BIGINT \
		--field path=STRING \
		--field pgroup=BIGINT \
		--field pid=BIGINT \
		--field resident_size=BIGINT \
		--field root=STRING \
		--field sgid=INT \
		--field start_time=BIGINT \
		--field state=INT \
		--field suid=INT \
		--field system_time=BIGINT \
		--field threads=INT \
		--field total_size=BIGINT \
		--field uid=INT \
		--field user_time=BIGINT \
		--field wired_size=INT \
		--field action=string \
		--prop service-url=$(PULSAR_ENDPOINT) \
		--prop admin-url=$(ADMIN_URL) \
		--prop topic=$(SINK) \
		--prop token=$(DS_TOKEN) \
		--prop format=json

filter:
	@decodable pipeline create \
		--name osquery_filter_noise \
  		--description "demo day osquery_filter_noise" \
  		- < sql/noise.sql

suspicious:
	@decodable pipeline create \
		--name suspicious_processes \
  		--description "demo day suspicious_processes" \
  		- < sql/suspicious.sql

cleanse:
	@decodable pipeline create \
		--name osquery_cleanse \
  		--description "demo day osquery_cleanse" \
  		- < sql/cleanse.sql

flow: osquery_processes filtered_processes suspicious_processes cleansed_processes source filter suspicious cleanse sink

active:
	decodable connection activate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="osquery_processes_pulsar")|.id ' )
	decodable pl activate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="osquery_filter_noise")|.id ' )
	decodable pl activate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="suspicious_processes")|.id ' )
	decodable pl activate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="osquery_cleanse")|.id ' )
	
deactive: 
	decodable connection deactivate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="osquery_processes_pulsar")|.id ' )
	decodable connection deactivate --force $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="osquery_processes_pulsar_sink")|.id ' )
	decodable pl deactivate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="osquery_filter_noise")|.id ' )
	decodable pl deactivate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="suspicious_processes")|.id ' )
	decodable pl deactivate --force $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="osquery_cleanse")|.id ' )
	
restart: deactive active

clean: deactive
	decodable connection delete $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="osquery_processes_pulsar")|.id ' )
	decodable connection delete $(shell decodable connection list -o json | jq -sr '.[] |select(.name=="osquery_processes_pulsar_sink")|.id ' )
	decodable pl delete $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="osquery_filter_noise")|.id ' )
	decodable pl delete $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="suspicious_processes")|.id ' )
	decodable pl delete $(shell decodable pl list -o json | jq -sr '.[] |select(.name=="osquery_cleanse")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="osquery_processes")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="filtered_processes")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="suspicious_processes")|.id ' )
	decodable stream delete $(shell decodable stream list -o json | jq -sr '.[] |select(.name=="cleansed_processes")|.id ' )
	