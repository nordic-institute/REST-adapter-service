# End-to-end Test for SOAP Service
This test uess Example Adapter to provide a SOAP service and verifies that the service can be consumed directly as SOAP 
service or as a REST service using the REST adapter. If necessary, more SOAP services can be added to the tests by using 
the request at `./hurl-verification/examples`


```+---------+        +---------------------+        +------------------+
+---------+  REST  +---------------------+  SOAP  +------------------+
| Client  |  --->  | rest-adapter-service|  --->  | example-adapter  |
| (REST)  |  <---  |   (SOAP -> REST)    |  <---  |     (SOAP)       |
+---------+        +---------------------+        +------------------+
```