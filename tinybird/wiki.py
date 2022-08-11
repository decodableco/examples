#!/bin/local/python

import time
import feedparser
import json
import os
import logging
import boto3
from dotenv import load_dotenv


def kinesis():
	return boto3.client('kinesis',region_name=os.getenv("REGION"))

def poll():
	feed=feedparser.parse('https://en.wikipedia.org/w/api.php?action=feedrecentchanges')
	entries = feed['entries']
	stream = os.getenv("KINESIS_STREAM")
	k = kinesis()

	for e in entries:
		print(json.dumps(e))
		k.put_record(
			StreamName=stream,
			Data=json.dumps(e),
			PartitionKey="-1")

def main():
	load_dotenv()
	logging.basicConfig(level=logging.INFO)

	while True:
		poll()
		time.sleep(10)

	



if __name__== "__main__":
	main()

	