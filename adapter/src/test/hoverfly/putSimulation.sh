#!/bin/sh
SIMULATION_FILENAME=$1
curl -i -X PUT -H 'Content-Type: application/json' -d @$SIMULATION_FILENAME http://localhost:8888/api/v2/simulation
