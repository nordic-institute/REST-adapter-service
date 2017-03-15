This example explains how to configure REST Gateway for a CRUD API. REST Gateway version 0.0.4 or later is required for this example.

### CRUD API

In this example a simple imaginary CRUD API is used for demonstrating the configuration of REST Gateway. The CRUD API has five operations that accept and return JSON:

* list bands
  * ``` [GET]  http://www.api.com/bands```
* get band
  * ``` [GET]  http://www.api.com/bands/{bandId}```
* add band
  * ``` [POST]  http://www.api.com/bands```
* update band
  * ``` [PUT]  http://www.api.com/bands/{bandId}```
* delete band
  * ``` [DELETE]  http://www.api.com/bands/{bandId}```

A band object has only two properties: ```id``` and ```name```.

```
{"id":1,"name":"Guns N' Roses"}
```

### Provider Gateway

All the five CRUP API operations must be added to ```WEB-INF/classes/providers.properties``` file.

```
0.id=FI_PILOT.GOV.1019125-0.Demo2Service.getBands.v1
0.url=http://www.api.com/bands/
0.verb=get
1.id=FI_PILOT.GOV.1019125-0.Demo2Service.getBand.v1
1.url=http://www.api.com/bands/
1.verb=get
2.id=FI_PILOT.GOV.1019125-0.Demo2Service.createBand.v1
2.url=http://www.api.com/bands/
2.verb=post
2.contenttype=application/json
3.id=FI_PILOT.GOV.1019125-0.Demo2Service.updateBand.v1
3.url=http://www.api.com/bands/
3.verb=put
3.contenttype=application/json
4.id=FI_PILOT.GOV.1019125-0.Demo2Service.deleteBand.v1
4.url=http://www.api.com/bands/
4.verb=delete
```

```WEB-INF/classes/provider-gateway.properties``` file should look like this:

```
wsdl.path=provider-gateway.wsdl
# Namespace for ServiceResponse
namespace.serialize=http://test.x-road.fi/producer
namespace.prefix.serialize=ts1
# Namespace for incoming ServiceRequest
namespace.deserialize=http://test.x-road.fi/producer
```

**N.B.** As long as Provider Gateway is not accessed through X-Road security server there's no need to pay attention to the WSDL file (```WEB-INF/classes/provider-gateway.wsdl```). The WSDL file is only for the security server and it's not needed by the Consumer Gateway.
### Consumer Gateway

All the five CRUP API operations must be added to ```WEB-INF/classes/consumers.properties``` file as well.

```
6.id=FI_PILOT.GOV.1019125-0.Demo2Service.getBands.v1
6.path=/bands/
6.verb=get
7.id=FI_PILOT.GOV.1019125-0.Demo2Service.getBand.v1
7.path=/bands/{resourceId}
7.verb=get
8.id=FI_PILOT.GOV.1019125-0.Demo2Service.createBand.v1
8.path=/bands/
8.verb=post
9.id=FI_PILOT.GOV.1019125-0.Demo2Service.updateBand.v1
9.path=/bands/{resourceId}
9.verb=put
10.id=FI_PILOT.GOV.1019125-0.Demo2Service.deleteBand.v1
10.path=/bands/{resourceId}
10.verb=delete
```

```WEB-INF/classes/consumer-gateway.properties``` file should look like this:

```
id.client=FI_PILOT.GOV.0245437-2.TestSystem
ss.url=http://localhost:8080/rest-gateway-0.0.4/Provider
# Namespace for ServiceRequest
namespace.serialize=http://test.x-road.fi/producer
namespace.prefix.serialize=ts1
# Namespace for incoming ServiceResponse
namespace.deserialize=http://test.x-road.fi/producer
```
**N.B.** ```ss.url``` property must contain the correct URL of the Provider Gateway.

**N.B.** It's very important that ```namespace.serialize``` property matches with ```namespace.deserialize``` property of Provider Gateway, and that ```namespace.deserialize``` property matches with ```namespace.serialize``` property of Provider Gateway.

### Using the services

Let's assume that Consumer Gateway is accessed at:

```
http://localhost:8080/rest-gateway-0.0.4/Consumer
```

And Provider Gateway is accessed at:

```
http://localhost:8080/rest-gateway-0.0.4/Provider
```

Using the services through Consumer Gateway:

* list bands
  * ``` [GET]  http://localhost:8080/rest-gateway-0.0.4/Consumer/bands```
* get band
  * ``` [GET]  http://localhost:8080/rest-gateway-0.0.4/Consumer/bands/{bandId}```
* add band
  * ``` [POST]  http://localhost:8080/rest-gateway-0.0.4/Consumer/bands```
  * Request body: ```{"name":"Guns N' Roses"}```
* update band
  * ``` [PUT]  http://localhost:8080/rest-gateway-0.0.4/Consumer/bands/1```
  * Request body: ```{"id":1,"name":"Guns N' Roses"}```
* delete band
  * ``` [DELETE]  http://localhost:8080/rest-gateway-0.0.4/Consumer/bands/1```