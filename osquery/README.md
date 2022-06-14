# OSQuery Log Routing



```mermaid
flowchart TD;
    os{{OSQuery+Extension}}-->Pulsar-->pc([Pulsar_Connection])
    
    pc-->opp[[osquery_processes]]-->SQL:osquery_filter_noise-->fp[[filtered_processes]]

    fp-->SQL:osquery_cleanse-->db[(Real-time Database)]
    fp-->SQL:suspicious_processes-->a>Alert]

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

