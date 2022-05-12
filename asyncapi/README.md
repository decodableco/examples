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
First create a ``.env`` file and place the contents below replacing the values with yours. You can ``source .env`` to put all these values in the environment.

```Makefile
ACCOUNT=<< your decodable account name >>
BUCKET=<< s3 bucket >>
REGION=<< aws region >>
ARN=<< AWS ARN ROLE >
```

All the commands are in the Makefile. Run them in order below.

* This command logs you into Decodable
```bash
make login
```

* This command lists all the streams defined in Decodable in your account. It then generates the AsyncAPI YAML document for all of the streams.
```bash
make async
```

* Optional: To filter streams with a prefix, you can use this command instead. Alteratively you can filter with postfix using `endswith()` function instead of `startswith()`.
```bash
decodable stream list -o json | \
		jq -r 'select(.name | startswith("my-prefix"))|.id' | 
		xargs -n1 decodable stream get -o json | \
		jq -s '{dp:.}' \
		> dp.json
```

* There should be a YAML document {YOUR_ACCOUNT}.yaml in the local directory. Run the next command to create the html document. This will create a `output` directory. When this command finishes, you can run `open output/index.html` to see it in your browser. The command uses this [html generator](https://github.com/asyncapi/html-template).
```bash
make html
```

* At this point, you consumers have enough information to create a S3 sink connection. Go [here](https://docs.decodable.co/docs/connector-reference-s3) for more information in configuring the S3 connection. This command will generate a script for you to run that will create the connection.

```bash
make s3
```



**NOTE** 
>All the commands in this example use the command line to subscribe to a data product in Decodable. If your intention is to build a self-service data products portal to consume data into customer domains, please use the Decodable's [API Reference](https://docs.decodable.co/reference/listpipelines) for REST APIs to automatically build sink connections.

**CONTACT US**
>For more information or help with building your data products portal, please contact use [here](https://www.decodable.co/contact) or join our slack community at the same link. 

