import requests
import json
import time
import sys
import traceback
from decodablepy import sender

def poll(url, config):
    try:
        response = requests.get(url)
        results = json.loads(response.text)
        events = []
        for j in results['Countries']:
            events.append(j)
            
        message = {}
        message["events"] = events

        # send(data={}, config={ "token": Empty, "account": Empty, "endpoint": Empty}, callback = lambda resp : print(resp)):
        sender.send(json.dumps(message), config, lambda resp : print(f"status code: {resp.status_code}"))

    except:
        print("Unexpected error:", sys.exc_info()[0])
        traceback.print_exc()


def main():
    url = 'https://api.covid19api.com/summary'

    config = {
        "token": sys.argv[1],
        "account": sys.argv[2],
        "endpoint": sys.argv[3]
    }

    while True:
        poll(url, config)
        time.sleep(3600)


if __name__== "__main__":
    main()