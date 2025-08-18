# End-to-end test for REST service

```
+---------+ REST +------------------------+ SOAP +------------------------+ REST +------------------+
| Client  | ---> |  rest-adapter-service  | ---> |  rest-adapter-service  | ---> |      WireMock    |
| (REST)  | <--- |        (Consumer)      | <--- |       (Provider)       | <--- |       (SOAP)     |
+---------+      +------------------------+      +------------------------+      +------------------+
```