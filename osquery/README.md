# OSQuery Log Routing
This example provides an OSQuery extenstion to write to Pulsar. Specifically to Datastax Astra Streaming but can be modified to write to another instance of Pulsar. The intention is to send the logs osquery captures to Pulsar to be read by Decodable. Decodable will filter and route the logs sending it to different consumers.

The diagram below shows the flow of data. 

```mermaid
flowchart TD;
    os{{OSQuery+Extension}}-->Pulsar-->pc([Pulsar_Connection])
    
    pc-->opp[[osquery_processes]]-->SQL:osquery_filter_noise-->fp[[filtered_processes]]

    fp-->SQL:osquery_cleanse-->db[(Real-time Database)]
    fp-->SQL:suspicious_processes-->a>Alert]

```

Start by installing osquery onto an operating system like AWS EC2. Then start osquery using the command like below.

```bash
sudo osqueryd \
    --extension pulsar.ext \
    --logger_plugin=pulsar_logger \
    --config_plugin=osquery_config \
    --allow_unsafe \
    --disable_events=false
```

`pulsar.ext` is an osquery extension in python. This will run osquery in the foreground but the preferred way is to run it as a daemon in the background. You will need a `.env` file to place your configuration. See the next section.

## Environment

Create a `.env` file and set the values.

```
DS_TOKEN=<< Datastax Token >>
SOURCE=<< Persistent Topic Name >>
SINK=<< Persistent Topic Name >>
PULSAR_ENDPOINT=<< PULSAR ENDPOINT >>
ADMIN_UR=<< Datastax Admin URL >>
```

## Python

Install the python mods.

```bash
pip install -r requirements.txt
```

## Decodable

```bash
make flow # creates the diagram above in Decodable
make active # activates the components created in "make flow"
```

Check in Decodable to see message flow after all of the components have started.

`make clean` to clean up you environment in Decodable.


