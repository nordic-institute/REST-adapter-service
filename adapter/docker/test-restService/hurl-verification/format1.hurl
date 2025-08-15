# Test .... endpoint
GET http://adapter:8080/rest-adapter-service/Consumer/FI_TEST/GOV/9876543-1/Wiremock/format1.v1
Accept: application/json

[Options]
retry: 20
retry-interval: 5000

# Expected response assertions
HTTP 200

# Validate the response structure and content
[Asserts]
status == 200

```json
[{"key":"Hello,WireMock!"},{"next":"Hello,WireMock!","last":"Hello,WireMock!"}]
```

#Actual: ["Hello,WireMock!",{"next":"Hello,WireMock!","last":"Hello,WireMock!"}]
