# Test .... endpoint
GET http://adapter:8080/rest-adapter-service/Consumer/FI_TEST/GOV/9876543-1/Wiremock/format6.v1
Accept: application/json

[Options]
retry: 20
retry-interval: 5000

# Expected response assertions
HTTP 200

# Validate the response structure and content
[Asserts]
status == 200

jsonpath "$[0].key1" == "content1"
jsonpath "$[0].key2" == "content2"
jsonpath "$[0].key3" == "content3"
jsonpath "$[1].key4" == "content4"
jsonpath "$[1].key5[0].key6" == "content6"

