import paho.mqtt.client as mqtt
import json
import os
import logging
import boto3
from dotenv import load_dotenv


def on_connect(client, userdata, flags, rc):
	 print("rc: " + str(rc))
 
def on_message(client, config, msg):
	response = {
		'topic': msg.topic,
		'payload': str(msg.payload.decode("utf-8")),
		'qos': msg.qos,
		'retain':msg.retain
	}
	config['p'].put_record(
		StreamName=config['t'],
		Data=json.dumps(response),
		PartitionKey="-1")
	print(response)
 
def on_publish(client, obj, mid):
	 print("mid: " + str(mid))
 
def on_subscribe(client, obj, mid, granted_qos):
	 print("Subscribed: " + str(mid) + " " + str(granted_qos))

def on_log(client, obj, level, string):
	 print(string)

def create_mqtt_client(config, mqtt_confg:dict):
	logging.info(f"Connecting to: {mqtt_confg}")
	mqttc = mqtt.Client(userdata=config)
	mqttc.on_message = on_message
	mqttc.on_connect = on_connect
	mqttc.on_publish = on_publish
	mqttc.on_subscribe = on_subscribe

	mqttc.username_pw_set(mqtt_confg["user"], mqtt_confg["pwd"])
	mqttc.connect(mqtt_confg["mqtt_host"], int(mqtt_confg["port"]))
	mqttc.subscribe(mqtt_confg["topic"], 0)
	return mqttc

def main():

	load_dotenv()
	logging.basicConfig(level=logging.INFO)

	kinesis_client = boto3.client('kinesis',region_name=os.getenv("REGION"))

	kinesis = {
		'p': kinesis_client,
		't': os.getenv("KINESIS_STREAM")
	}

	mqtt_config = {
		'mqtt_host':os.getenv("MQTT_HOST"), 
		'port': os.getenv("MQTT_PORT"), 
		'user': os.getenv("MQTT_USER"), 
		'pwd': os.getenv("MQTT_PASSWORD"), 
		'topic': os.getenv("TOPIC")
	}

	mqttc = create_mqtt_client(mqtt_confg=mqtt_config, config=kinesis)

	rc = 0
	while rc == 0:
		rc = mqttc.loop()


if __name__== "__main__":
	main()

	