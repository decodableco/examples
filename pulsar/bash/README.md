# Client

## Brew Install CLI

```bash
brew install apache-pulsar
```

## Makefile

```bash
make source # creates the pulsar source connection
make produce # produces to pulsar
```

## Client configuration

```bash
export PULSAR_CLIENT_CONF=~/.sncloud/client.conf

echo '
webServiceUrl=https://free.o-whe2d.snio.cloud
brokerServiceUrl=pulsar+ssl://free.o-whe2d.snio.cloud:6651
authPlugin=org.apache.pulsar.client.impl.auth.oauth2.AuthenticationOAuth2
authParams={"privateKey":"file:///Users/hubertdulay/development/decodable-demos/examples/pulsar/bash/o-whe2d-free.json", "issuerUrl":"https://auth.streamnative.cloud/", "audience":"urn:sn:pulsar:o-whe2d:free"}
tlsAllowInsecureConnection=false
tlsEnableHostnameVerification=true
' > $PULSAR_CLIENT_CONF

# with PULSAR_CLIENT_CONF set, the above config file will be used
pulsar-admin tenants list
```

## Producer

```bash
# From the left navigation pane of the StreamNative Cloud Manager, click Manage > Service Accounts > Download  to download the Oauth2 key file to the local path. Then, replace the `YOUR-KEY-FILE-PATH` parameter with the local path for your Oauth2 key file.
# export TENANT_NAMESPACE_TOPIC=persistent://public/default/user
pulsar-client \
   --url pulsar+ssl://free.o-whe2d.snio.cloud:6651 \
    --auth-plugin org.apache.pulsar.client.impl.auth.oauth2.AuthenticationOAuth2 \
    --auth-params '{"privateKey":"file:///Users/hubertdulay/development/decodable-demos/examples/pulsar/bash/o-whe2d-free.json", "issuerUrl":"https://auth.streamnative.cloud/", "audience":"urn:sn:pulsar:o-whe2d:free"}' \
    produce \
    -f $(pwd)/data.json "persistent://public/default/user"


```