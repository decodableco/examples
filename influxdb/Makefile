include .env

test:
	telegraf --config https://us-east-1-1.aws.cloud2.influxdata.com/api/v2/telegrafs/0a8fe1d61a55a000


sample:
	cat sample.json | base64 

deploy:
	rm -rf ./package
	pip install --target ./package influxdb_client
	cd package && zip -r ../influx-deployment-package.zip .
	zip influx-deployment-package.zip lambda_function.py
	aws lambda update-function-code --function-name influxdb --zip-file fileb://influx-deployment-package.zip


