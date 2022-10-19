import json
import urllib.parse
import boto3
import base64, io
import requests

print('Loading function')

s3 = boto3.client('s3')
kinesis = boto3.client('kinesis','us-west-2')


def lambda_handler(event, context):

    # Get the object from the event and show its content type
    for record in event['Records']:
        bucket = record['s3']['bucket']['name']
        key = urllib.parse.unquote_plus(record['s3']['object']['key'], encoding='utf-8')
        try:
            response = s3.get_object(Bucket=bucket, Key=key)
            body = response['Body'].read()
            for record in body.decode('utf-8').split('\n')[:-1]:
                kinesis.put_record(
                    StreamName='s3-kinesis',
                    Data=record,
                    PartitionKey="-1")
            
            return "success"
            
        except Exception as e:
            print(e)
            print('Error getting object {} from bucket {}. Make sure they exist and your bucket is in the same region as this function.'.format(key, bucket))
            raise e
