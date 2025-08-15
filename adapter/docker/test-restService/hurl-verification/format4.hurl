# Test .... endpoint

# using this works
GET http://adapter:8080/rest-adapter-service/Consumer/FI_TEST/GOV/9876543-1/Wiremock/format4.v1

Accept: application/json

[Options]
retry: 40
retry-interval: 5000

# Expected response assertions
HTTP 200

# Validate the response structure and content
[Asserts]
status == 200

```json
{[{"key1":"content1","key2":"content2"},{"key3":"content3","key4":"content4"}]}
```

