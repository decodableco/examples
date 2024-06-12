# Getting Started With PyFlink On Decodable

This example project shows how to run PyFlink jobs on Decodable.

By deploying your PyFlink jobs as [Custom Pipelines](https://docs.decodable.co/pipelines/create-pipelines-using-your-own-apache-flink-jobs.html) onto Decodable, you can solely focus on implementing your job,
while leaving all the aspects of running the job, like provisioning Flink clusters and the underlying hardware,
keeping them secure and up-to-date, scaling them, monitoring and observing them, to the fully-managed Decodable platform.

## Prerequisites

You'll need the following things in place to run this example:

* A free Decodable account ([sign up](https://app.decodable.co/-/accounts/create))
* The [Decodable CLI](https://docs.decodable.co/cli.html)
* [GNU Make](https://www.gnu.org/software/make/)
* A Kafka cluster which can be accessed via Decodable; For instance, the free tier of [Upstash](https://upstash.com/) can be used

Make sure you are logged into your Decodable account on the CLI by running `decodable login`.

## Description

This example shows how to run a simple [PyFlink](https://nightlies.apache.org/flink/flink-docs-release-1.18/docs/dev/python/overview/) job on Decodable.
With the help of the built-in DataGen connector, a random `Todo` event is emitted per second.
A user-defined function (UDF) is used for enriching each event with user information retrieved from a remote REST API,
leveraging the `requests` and `jmespath` 3rd-party libraries.
The enriched events are sent to a Kafka topic.

## Running the Example

After checking out the project, provide the broker address and credentials for your Kafka cluster in the files _.secret\_kafka\_bootstrap\_servers_, ._secret\_kafka\_user\_name_, and _secret\_kafka\_password_, respectively.
If your cluster is using another securiy protocol than SASL_SSL with SCRAM-SHA-256,
adjust the connector configuration of the `enriched_todos` table in _main.py_ accordingly.

Next, build the PyFlink job and deploy it to your Decodable account:

```
$ make
$ make deploy
```

Take note of the id generated for the `pyflink_on_decodable` pipeline.
Then activate this pipeline using the Decodable CLI:

```
$ decodable pipeline activate <id>
```

Once the pipeline is running (use `decodable pipeline get <id>` to query its state),
you can observe the enriched `Todo` events in the Kafka topic, for instance via the web console when using Upstash.

## Clean-Up

To shut down the pipeline and clean up all the resources in your Decodable account,
run the following commands:

```
$ decodable pipeline deactivate <id>
$ decodable pipeline delete <id>

# Obtain secret ids via decodable secret list
$ decodable secret delete <id1>
$ decodable secret delete <id2>
$ decodable secret delete <id3>
```
