#!/bin/bash

# NOTE: Uses the decodable CLI to get the url for the rest connection.
# If you don't have the CLI installed go into the Decodable UI
# and check the configuration of the rest-source connection
# to manually set the URL here: 
#
# api_endpoint='https://<XY>.api.data.decodable.co/v1alpha2/connections/<ID>/events'
#
api_endpoint=`decodable -p private query --kind connection --name "rest-source" | yq '.status.properties.url'`

fruits=("Apple" "Banana" "Cherry" "Fig" "Grape" "Kiwi" "Lemon" "Raspberry" "Mango" "Blueberry" "Apricot" "Melon" "Cranberry")
max_num_events=10000
max_fruit_count=10
min_delay_secs=0.25
max_delay_secs=1.5
for ((i=0; i<max_num_events; i++)); do
  fruit=${fruits[$RANDOM % ${#fruits[@]}]}
  count=$(( (RANDOM % $max_fruit_count) + 1 ))
  payload=$(cat <<EOF
    {
        "events": [
        {
            "name": "$fruit",
            "count": $count
        }
        ]
    }
EOF
)
  echo "sending fruit intake event:"
  echo $payload
  curl -X POST "$api_endpoint" \
       -H "Content-Type: application/json" \
       -H 'Authorization: Bearer JGUocmVU' \
       -d "$payload"
  echo ""
  echo "pausing a bit ..."
  sleep $(awk -v min=$min_delay_secs -v max=$max_delay_secs 'BEGIN{srand(); print min+rand()*(max-min)}')
done
