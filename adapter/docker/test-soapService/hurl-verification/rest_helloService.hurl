GET http://adapter:8080/rest-adapter-service/Consumer/helloService?name=Test
Accept: application/json

[Options]
retry: 20
retry-interval: 5000

HTTP 200

[Asserts]
status == 200

jsonpath "$.message" == "Hello Test! Greetings from adapter server!"


