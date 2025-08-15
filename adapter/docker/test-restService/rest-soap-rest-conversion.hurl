# Test helloService endpoint with TestName parameter
GET http://adapter:8080/rest-adapter-service/Consumer/FI_TEST/GOV/9876543-1/Wiremock/helloService.v1
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
[{"content2":"Hello, WireMock!","content":"Hello, WireMock!"},{"next":"Hello, WireMock!","last":"Hello, WireMock!"}]
```
