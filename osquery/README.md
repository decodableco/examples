# OSQuery Log Routing



```mermaid
flowchart TD;
    os{{OSQuery+Extension}}-->Pulsar-->pc([Pulsar_Connection])
    
    pc-->opp[[osquery_processes]]-->osquery_filter_noise-->fp[[filtered_processes]]

    fp-->osquery_cleanse-->db[(Real-time Database)]
    fp-->suspicious_processes-->a>Alert]

```


## Environment

Create a `.env` file and set the values.

```
DS_TOKEN=<< Datastax Token >>
TOPIC=<< Persistent Topic Name >>
PULSAR_ENDPOINT=<< PULSAR ENDPOINT >>
ADMIN_UR=<< Datastax Admin URL >>
```

## python
Pulsar require python 3.8

https://osquery.io/downloads/official/5.2.3

