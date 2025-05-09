#!/bin/sh
# get information from hoverfly instance, for example # ./getCommand.sh journal
# pretty prints json with "jq", so that is required: https://stedolan.github.io/jq/tutorial/
COMMAND=$1
curl -X GET -H 'Content-Type: application/json' http://localhost:8888/api/v2/$COMMAND | jq .
