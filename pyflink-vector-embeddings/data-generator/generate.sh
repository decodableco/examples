#!/bin/sh

JSON_FILE=/home/data/data_sample.json
SEND_DELAY=1.0
HOSTNAME=review-app:8080

while read -r json
do
    review=`echo $json | jq '{itemId: .asin, reviewText: .text}'`
    echo "📤 posting user review via REST API"
    curl --location $HOSTNAME/api/v1/reviews --header 'Content-Type: application/json; Charset=utf-8' --data "$review"
    printf '\n⏱️ delaying for %ss\n' $SEND_DELAY
    sleep $SEND_DELAY
done < $JSON_FILE
