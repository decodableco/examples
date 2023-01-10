import json
import influxdb_client, os, base64
from influxdb_client import InfluxDBClient, Point, WritePrecision
from influxdb_client.client.write_api import SYNCHRONOUS


# GetRecords, GetShardIterator, DescribeStream, ListShards, and ListStreams Actions on your stream in IAM.

def lambda_handler(event, context):
    # TODO implement

    token = os.environ.get("INFLUXDB_TOKEN")
    org = os.environ.get("INFLUXDB_ORG")
    url = os.environ.get("INFLUXDB_URL")
    client = influxdb_client.InfluxDBClient(url=url, token=token, org=org)

    bucket=os.environ.get("INFLUXDB_BUCKET")

    write_api = client.write_api(write_options=SYNCHRONOUS)

    for record in event['Records']:
        print(record)
        string = base64.decodebytes(record["kinesis"]["data"].encode('utf-8')).decode('utf-8')
        data = json.loads(string)
        print(data)

        point = (
            Point("event")
            .tag("http_event", data['req_path'])
            .field("_raw", data['_raw'])
            .field("_time", data['_time'])
            .field("req_method", data['req_method'])
            .field("req_path", data['req_path'])
            .field("req_proto", data['req_proto'])
            .field("req_flags", data['req_flags'])
            .field("resp_status", data['resp_status'])
            .field("resp_size", data['resp_size'])
        )
        write_api.write(bucket=bucket, org="dev", record=point)
    

    return {
        'statusCode': 200,
        'body': json.dumps('Hello from Lambda!')
    }
