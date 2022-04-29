# Publishing Data Products with Decodable and AsyncAPI
AsyncAPI is an open source initiative that seeks to improve the current state of Event-Driven Architectures (EDA). AsyncAPI provides a standard way of describing asynchronous data (streaming data) in a way that extends OpenAPI that describes REST APIs. The goal is to make streaming architectures as easy as REST APIs. You can extend AsyncAPI to add self-service capabilities that will enable easy integration and consumption of data products published in Decodable.


## Getting Started

### Requirements
* Setup an Account in Decodable.co & Install the Decodable CLI
You can follow the steps [here](https://docs.decodable.co/docs/setup)

* Install jq command line JSON processor
```bash
pip install jq
```

* Install AsyncAPI Generator
```bash
npm install -g @asyncapi/generator
```

* Install Jinja
```bash
pip install jinja-cli
```

* Optional Install yq to parse YAML
```bash
pip install yq
```

### Makefile

```bash
make login
```

```bash
make async
```

```bash
make html
```

```bash
SINK_NAME=s3-sink
ACCOUNT=you-decodable-account
STREAM_ID=xxxxxxx
BUCKET=my-bucket
DIRECTORY=my-directory
FORMAT=json
REGION=us-west-2
ROLE=arn:aws:iam::xxxxxxx:role/xxxxxxxx

decodable conn create \
		--name $(SINK_NAME) \
		--connector s3 \
		--type sink \
		--stream-id $(STREAM_ID) \
		--field ts=TIMESTAMP \
		--field status=INT \
		--field method=STRING \
		--field path=STRING \
		--prop bucket=$(BUCKET) \
		--prop directory=$(DIRECTORY) \
		--prop format=$(FORMAT) \
		--prop region=$(REGION) \
		--prop role-arn=$(ROLE)
```