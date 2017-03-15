### Prerequisites

Before configuring REST Gateway Provider for X-Road the following conditions must be met:

* organization must be registered as X-Road member
* Security Server installation must be completed
* subsystem that's used for publishing REST services must be registered
* WSDL description of the REST services to be published must be available

### Configuration

General settings are configured through ```WEB-INF/classes/provider-gateway.properties``` configuration file. All the general properties are mandatory.

<table>
          <tbody>
            <tr>
              <th>Property</th>
              <th>Required</th>
              <th>Default</th>
              <th>Description</th>
            </tr>
            <tr>
              <td>wsdl.path</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Path or filename of the WSDL file. If only filename is given, the file must be located in WEB-INF/classes folder.</td>
            </tr>
            <tr>
              <td>namespace.deserialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace that's used for deserializing incoming SOAP requests. Can be overridden for each service. **N.B.** Must match the namespace used in the WSDL description.</td>
            </tr>
            <tr>
              <td>namespace.serialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace that's used for serializing outgoing SOAP responses. Can be overridden for each service. **N.B.** Must match the namespace used in the WSDL description.</td>
            </tr>
            <tr>
              <td>namespace.prefix.serialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace prefix that's used for serializing outgoing SOAP responses. Can be overridden for each service.</td>
            </tr>
</tbody>
</table>

REST services to be published are configured through ```WEB-INF/classes/providers.properties``` configuration file. Each service has 10 properties of which 2 are mandatory. Each property must be prefixed with the number of the service, e.g. ```0.id```, ```0.url```. The numbering starts from zero.

<table>
          <tbody>
            <tr>
              <th>Property</th>
              <th>Required</th>
              <th>Default</th>
              <th>Description</th>
            </tr>
            <tr>
              <td>id</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Identifier of the X-Road service : instance.memberClass.memberId.subsystem.service.version</td>
            </tr>
            <tr>
              <td>url</td>
              <td>&#42;</td>
              <td>-</td>
              <td>URL of the REST service.</td>
            </tr>
            <tr>
              <td>verb</td>
              <td></td>
              <td>GET</td>
              <td>HTTP verb that's used in the service call. Supported values: GET, POST, PUT and DELETE.</td>
            </tr>
            <tr>
              <td>contenttype</td>
              <td></td>
              <td>-</td>
              <td>Content-type HTTP header that's used in the service call. Required if HTTP verb is POST, PUT or DELETE and HTTP request contains a request body.</td>
            </tr>
            <tr>
              <td>accept</td>
              <td></td>
              <td>-</td>
              <td>Accept HTTP header that's used in the service call.</td>
            </tr>
            <tr>
              <td>response.attachment</td>
              <td></td>
              <td>false</td>
              <td>Return REST response as SOAP attachment. If response is returned as SOAP attachment, Consumer Gateway ignores the value of HTTP Accept header and returns the response in the original format.</td>
            </tr>
            <tr>
              <td>request.xrdheaders</td>
              <td></td>
              <td>true</td>
              <td>Pass X-Road SOAP headers to REST service via HTTP headers.</td>
            </tr>
            <tr>
              <td>namespace.deserialize</td>
              <td></td>
              <td>-</td>
              <td>Namespace that's used for deserializing incoming SOAP requests. If not defined, default value from provider-gateway.properties is used.</td>
            </tr>
            <tr>
              <td>namespace.serialize</td>
              <td></td>
              <td>-</td>
              <td>Namespace that's used for serializing outgoing SOAP responses. If not defined, default value from provider-gateway.properties is used.</td>
            </tr>
            <tr>
              <td>namespace.prefix.serialize</td>
              <td></td>
              <td>-</td>
              <td>Namespace prefix that's used for serializing outgoing SOAP responses. If not defined, default value from provider-gateway.properties is used.</td>
            </tr>
</tbody>
</table>

### Publishing to X-Road

Services provided through Provider Gateway are published to X-Road by adding the URL of the WSDL description to the Security Server and configuring necessary access rights to the services.

WSDL description URL:
```
http://myserver.com/rest-gateway-x.x.x/Provider?wsdl
```
### Example

General configuration at ```WEB-INF/classes/provider-gateway.properties```:

```
wsdl.path=provider-gateway.wsdl
# Namespace for ServiceResponse
namespace.serialize=http://test.x-road.fi/producer
namespace.prefix.serialize=ts1
# Namespace for incoming ServiceRequest
namespace.deserialize=http://test.x-road.fi/producer
```

WSDL description at ```WEB-INF/classes/provider-gateway.wsdl```:
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

Configuration at ```WEB-INF/classes/providers.properties```:

```
0.id=FI-DEV.GOV.123456-7.TestSystem.getRandom.v1
0.url=http://example.com/service/endpoint
0.verb=get
```