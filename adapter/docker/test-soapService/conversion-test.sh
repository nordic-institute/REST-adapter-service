#!/bin/bash
set -e

echo "Running REST-adapter conversion test SOAP-REST-SOAP"

projectName="rest-adapter-service"

docker compose -f docker-compose.yaml -p "rest-adapter-service" up -d
trap 'RV=$?; docker compose -f docker-compose.yaml -p $projectName down -v; exit $RV' EXIT

docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/soap_helloService.hurl
docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/rest_helloService.hurl
