#!/bin/bash
set -e

echo "Running REST-adapter conversion test"

projectName="rest-adapter-service"

docker compose -f docker-compose.yaml -p "rest-adapter-service" up -d
trap 'RV=$?; docker compose -f docker-compose.yaml -p $projectName down -v; exit $RV' EXIT
sleep 5

#docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/format1.hurl
#docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/format2.hurl
#docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/format3.hurl
#docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/format4.hurl
#docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/format5.hurl
docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/format6.hurl

#docker exec $projectName-hurl-1 hurl -k --test /tmp/test/hurl-verification/soap_format1.hurl

