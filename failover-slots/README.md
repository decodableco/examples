# Failover Slots with Postgres 17

This example shows the usage of Postgres 17 failover replication slots with Postgres' SQL-based interface to logical replication, as well as with Decodable.
It accompanies [this blog post](https://www.decodable.co/blog/failover-replication-slots-with-postgres-17).

To run this example, you’ll need Docker. For running the Decodable-specific part, you'll also need to have the following:

* A free Decodable account
* The Decodable CLI installed on your machine
* A free ngrok account

Retrieve your auth token from the ngrok web UI and put it into a file _.env_ like so:

```
NGROK_AUTHTOKEN=<your token>
```

Start everything by running:

```
docker compose up
```

Then, follow the instructions from the blog post.
