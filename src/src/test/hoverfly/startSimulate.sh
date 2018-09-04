#!/bin/sh
# hoverfly set simulate mode
curl -i -X PUT -H 'Content-Type: application/json' http://localhost:8888/api/v2/hoverfly/mode --data '{
    "mode": "simulate"
}'
