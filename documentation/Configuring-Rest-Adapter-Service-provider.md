### Prerequisites

Before configuring Rest Adapter Service Provider for X-Road the following conditions must be met:

* organization must be registered as X-Road member
* Security Server installation must be completed
* subsystem that's used for publishing REST services must be registered
* WSDL description of the REST services to be published must be available

### Configuration

General settings are configured through ```provider-gateway.properties``` configuration file. All the general properties are mandatory.

| Property                  | Required | Default | Description                                                                                     |
|---------------------------|----------|---------|-------------------------------------------------------------------------------------------------|
| wsdl.path                | &#42;    | -       | File reading is first attempted from WEB-INF/classes directory (either inside packaged war, or in exploded war directory), and then from filesystem using the provided filename or path. |
| namespace.deserialize    | &#42;    | -       | Namespace that's used for deserializing incoming SOAP requests. Can be overridden for each service. **N.B.** Must match the namespace used in the WSDL description. |
| namespace.serialize      | &#42;    | -       | Namespace that's used for serializing outgoing SOAP responses. Can be overridden for each service. **N.B.** Must match the namespace used in the WSDL description. |
| namespace.prefix.serialize| &#42;    | -       | Namespace prefix that's used for serializing outgoing SOAP responses. Can be overridden for each service. |

REST services to be published are configured through ```providers.properties``` configuration file. Each service has 10 properties of which 2 are mandatory. Each property must be prefixed with the number of the service, e.g. ```0.id```, ```0.url```. The numbering starts from zero.

| Property                  | Required | Default | Description                                                                                                                                                                                    |
|---------------------------|----------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| id                       | &#42;    | -       | Identifier of the X-Road service : instance.memberClass.memberId.subsystem.service.version                                                                                                     |
| url                      | &#42;    | -       | URL of the REST service.                                                                                                                                                                       |
| verb                     |          | GET     | HTTP verb that's used in the service call. Supported values: GET, POST, PUT and DELETE.                                                                                                        |
| contenttype              |          | -       | Content-type HTTP header that's used in the service call. Required if HTTP verb is POST, PUT or DELETE and HTTP request contains a request body.                                               |
| accept                   |          | -       | Accept HTTP header that's used in the service call.                                                                                                                                            |
| response.attachment      |          | false   | Return REST response as SOAP attachment. If response is returned as SOAP attachment, Consumer Gateway ignores the value of HTTP Accept header and returns the response in the original format. |
| request.xrdheaders       |          | true    | Pass X-Road SOAP headers to REST service via HTTP headers.                                                                                                                                     |
| namespace.deserialize    |          | -       | Namespace that's used for deserializing incoming SOAP requests. If not defined, default value from provider-gateway.properties is used.                                                        |
| namespace.serialize      |          | -       | Namespace that's used for serializing outgoing SOAP responses. If not defined, default value from provider-gateway.properties is used.                                                         |
| namespace.prefix.serialize|          | -       | Namespace prefix that's used for serializing outgoing SOAP responses. If not defined, default value from provider-gateway.properties is used.                                                  |

### Publishing to X-Road

Services provided through Provider Gateway are published to X-Road by adding the URL of the WSDL description to the Security Server and configuring necessary access rights to the services.

WSDL description URL:
```
http://myserver.com/rest-adapter-service/Provider?wsdl
```
### Example

General configuration at ```provider-gateway.properties```:

```
wsdl.path=provider-gateway.wsdl
# Namespace for ServiceResponse
namespace.serialize=http://test.x-road.fi/producer
namespace.prefix.serialize=ts1
# Namespace for incoming ServiceRequest
namespace.deserialize=http://test.x-road.fi/producer
```

WSDL description at ```provider-gateway.wsdl```:
```
<wsdl:definitions xmlns:soap="http://schemas.xmlsoap.org/wsdl/soap/"
                  xmlns:tns="http://test.x-road.fi/producer"
                  .
                  .
                  name="vrk-testService" targetNamespace="http://test.x-road.fi/producer">
    <wsdl:types>
        <xsd:schema targetNamespace="http://test.x-road.fi/producer">
            <xsd:element name="getRandom">
                .
                .
            </xsd:element>
            <xsd:element name="getRandomResponse">
                .
                .
            </xsd:element>
    </wsdl:types>
    .
    .
    .        
    <wsdl:operation name="getRandom">
        <soap:operation soapAction="" style="document"/>
        <id:version>v1</id:version>
        .
        .
    </wsdl:operation>
</wsdl:definitions>
```

Organization's X-Road member identifier registered by the X-Road center:
```
FI-DEV.GOV.123456-7
```

Subsystem that's used for publishing the service through X-Road (optional):
```
TestSystem
```

Service details:
```
Service name: getRandom
Service version: v1
Service URL: http://example.com/service/endpoint
```

Configuration at ```providers.properties```:

```
0.id=FI-DEV.GOV.123456-7.TestSystem.getRandom.v1
0.url=http://example.com/service/endpoint
0.verb=get
```
