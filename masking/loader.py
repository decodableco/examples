#!/usr/local/bin/ python

import csv, json
import requests
import os, sys
from dotenv import load_dotenv


# Function to convert a CSV to JSON
# Takes the file paths as arguments
def make_json(csv_file, token, ep):

	# Open a csv reader called DictReader
	with open(csv_file, encoding='utf-8') as csvf:
		csvReader = csv.DictReader(csvf)
		
		# Convert each row into a dictionary
		# and add it to data
		for row in csvReader:
			message = json.dumps({
				"events": [ row ]
			})

			print(message)

			headers = {
				"Accept": "application/json",
				"Content-Type": "application/json",
				"Authorization": "Bearer {0}".format(token)
			}

			url = "https://{0}.api.decodable.co{1}".format(os.getenv("ACCOUNT"), ep)

			response = requests.post(url, headers=headers, data=message)

			print(response.text)

if __name__ == "__main__":
	
	csv_file = sys.argv[1]
	token = sys.argv[2]
	ep = sys.argv[3]

	load_dotenv()
	make_json(csv_file, token, ep)
