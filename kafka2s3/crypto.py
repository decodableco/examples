#!/bin/local/python

import time
import logging
import json
import os
from dotenv import load_dotenv
import urllib.request

from kafka import KafkaProducer
from kafka.errors import KafkaError
import json

def poll(producer, topic):
	url = "https://api.nomics.com/v1/currencies/ticker?key={}&ids=BTC,ETH,XRP,DOGE,SHIB,BCH,BSV,LINK,LTC&interval=1d,30d&per-page=1000&page=1".format(os.getenv("NOMIC_API_KEY"))
	msgs = json.loads(urllib.request.urlopen(url).read())

	for msg in msgs:
		print(json.dumps(msg))
		key=bytes(msg['id'].encode('utf-8'))
		producer.send(topic, key=key, value=msg)
		producer.flush()


def main():
	load_dotenv()
	logging.basicConfig(level=logging.INFO)

	producer = KafkaProducer(bootstrap_servers=[os.getenv("BOOTSTRAP")],value_serializer=lambda v: json.dumps(v).encode('utf-8'))
	topic='crypto'

	while True:
		poll(producer, topic)
		time.sleep(20)

	



if __name__== "__main__":
	main()