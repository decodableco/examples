#!/usr/bin/env python

import pulsar,time,sys

class Consumer:

    def __init__(self, service_url, topic, token, consumer):
        self.client = pulsar.Client(service_url, authentication=pulsar.AuthenticationToken(token))
        self.consumer = self.client.subscribe(topic, consumer)

    def consume(self):
        waitingForMsg = True
        while waitingForMsg:
            try:
                msg = self.consumer.receive(2000)
                print("Received message '{}' id='{}'".format(msg.data(), msg.message_id()))
                self.consumer.acknowledge(msg)

                # waitingForMsg = False
            except:
                time.sleep(1)
                continue

    
    def __del__(self):
        self.client.close()



if __name__ == "__main__":
    service_url = sys.argv[1]
    topic = sys.argv[2]
    token = sys.argv[3]
    consumer = sys.argv[4]

    c = Consumer(service_url, topic, token, consumer)
    c.consume()