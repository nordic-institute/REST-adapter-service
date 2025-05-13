### Table of contents

* [Principle of Operation](Rest-Adapter-Service-principles.md#principle-of-operation)
  * [Consumer Gateway](Rest-Adapter-Service-principles.md#consumer-gateway)
  * [Provider Gateway](Rest-Adapter-Service-principles.md#provider-gateway)
* [Installation](Rest-Adapter-Service-principles.md#installation)
  * [Consumer Gateway](Rest-Adapter-Service-principles.md#consumer-gateway-1)
  * [Provider Gateway](Rest-Adapter-Service-principles.md#provider-gateway-1)
* [Consumer Gateway Configuration](Rest-Adapter-Service-principles.md#consumer-gateway-configuration)
* [Provider Gateway Configuration](Rest-Adapter-Service-principles.md#provider-gateway-configuration)
* [Usage Examples](Rest-Adapter-Service-principles.md#usage)
  * [REST API for City of Helsinki Service Map - List of Organizations](Rest-Adapter-Service-principles.md#rest-api-for-city-of-helsinki-service-map---list-of-organizations)
    * [Consumer Gateway](Rest-Adapter-Service-principles.md#consumer-gateway-2)
    * [Provider Gateway](Rest-Adapter-Service-principles.md#provider-gateway-2)
  * [REST API for City of Helsinki Service Map - Single Organization](Rest-Adapter-Service-principles.md#rest-api-for-city-of-helsinki-service-map---single-organization)
    * [Consumer Gateway](Rest-Adapter-Service-principles.md#consumer-gateway-3)
    * [Provider Gateway](Rest-Adapter-Service-principles.md#provider-gateway-3)
  * [Finnish Library Directory](Rest-Adapter-Service-principles.md#library-directory)
    * [Consumer Gateway](Rest-Adapter-Service-principles.md#consumer-gateway-4)
    * [Provider Gateway](Rest-Adapter-Service-principles.md#provider-gateway-4)
  * [Finnish Patent and Registration Office - Business Information Search](Rest-Adapter-Service-principles.md#finnish-patent-and-registration-office---business-information-search)
    * [Consumer Gateway](Rest-Adapter-Service-principles.md#consumer-gateway-5)
    * [Provider Gateway](Rest-Adapter-Service-principles.md#provider-gateway-5)
  * [Finnish Patent and Registration Office - Get Company](Rest-Adapter-Service-principles.md#finnish-patent-and-registration-office---get-company)
    * [Consumer Gateway](Rest-Adapter-Service-principles.md#consumer-gateway-6)
    * [Provider Gateway](Rest-Adapter-Service-principles.md#provider-gateway-6)
  * [Finto : Finnish Thesaurus and Ontology Service - Search](Rest-Adapter-Service-principles.md#finto--finnish-thesaurus-and-ontology-service---search)
    * [Consumer Gateway](Rest-Adapter-Service-principles.md#consumer-gateway-7)
    * [Provider Gateway](Rest-Adapter-Service-principles.md#provider-gateway-7)

### Principle of Operation

This is Rest Adapter Service component that sits between X-Road security server and a REST client or service ([diagram](https://github.com/nordic-institute/REST-adapter-service/raw/master/images/message-sequence_rest-gateway-0.0.4.png)). The component implements X-Road v4.0 [SOAP profile](https://github.com/nordic-institute/X-Road/blob/develop/doc/Protocols/pr-mess_x-road_message_protocol.md) and it's compatible with X-Road v6.4 and above. 

Rest Adapter has two parts: _Consumer Gateway_ and _Provider Gateway._ It is possible to use either only Consumer Gateway, only Provider Gateway, or both.

![different adapter usage scenarios](../images/restgw-use-cases.png "Different adapter usage scenarios")

* (A) using both Consumer and Provider Gateways
  * when both the client and the server are REST/JSON, but the messages need to go through X-Road
  * when end to end encryption is needed
* (B) using only Consumer Gateway
  * when the service is SOAP/XML, but a client needs to access it through REST/JSON
* (C) using only Provider Gateway
  * when a REST/JSON service needs to be published in X-Road for SOAP/XML clients

**N.B.** Use of alternative A is no longer required since X-Road has a built-in support for consuming and producing REST services starting from version 6.21.0. [More information.](https://github.com/nordic-institute/X-Road/blob/develop/doc/Protocols/pr-rest_x-road_message_protocol_for_rest.md)

The component is tested with X-Road v6.4 and it includes the following features:

* **Provider Gateway** : access REST services (JSON, XML) via WSDL defined X-Road services
  * supported HTTP verbs: ```GET```, ```POST```, ```PUT``` and ```DELETE```
  * WSDL must be created manually
  * REST response can be wrapped in SOAP body or SOAP attachment
  * X-Road SOAP-headers are passed via HTTP headers (X-XRd-Client, X-XRd-Service, X-XRd-UserId, X-XRd-MessageId)
* **Consumer Gateway** : access WSDL defined X-Road services in a RESTful manner
  * full support for services published through Provider Gateway
  * limited support for other SOAP/XML services. See [Consumer Gateway](#consumer-gateway) for details
  * supported HTTP verbs: ```GET```, ```POST```, ```PUT``` and ```DELETE```
  * reformatting of resource links
  * response's content type is given using Accept header (```text/xml```, ```application/json```)
    * if Provider Gateway returns the response as SOAP attachment, the value of HTTP Accept header is ignored and the response is returned in the original format
  * X-Road SOAP headers are passed via HTTP headers (X-XRd-UserId, X-XRd-MessageId)
  * support for browser-based access (**N.B.** reformatting of resource links not supported)
    * service id and X-Road SOAP headers can be given as URL parameters
	* can be disabled through configuration
* automatic conversions: JSON -> XML, XML -> JSON, JSON-LD -> XML, XML -> JSON-LD
  * [Round-trip](http://en.wikipedia.org/wiki/Round-trip_format_conversion) JSON-XML-JSON conversion
  * support for JSON-LD
  * JSON -> XML conversion (for requests and responses) produces XML with undefined child element ordering. If client or service is strict about ordering (maybe because it validates the messages against the schema), this may be a problem.
* use of ```<request>``` / ```<response>``` wrapper tags can be configured for each service
  * service provider decides whether to use wrappers or not, consumer end must be configured accordingly
* support for encryption/decryption of message content. More information and instructions for configuration can be found in [encryption documentation](Encryption.md).
* new REST services can be added through configuration - no coding needed

#### Consumer Gateway

Consumer Gateway accepts HTTP GET, POST, PUT and DELETE requests, and it translates them to SOAP messages following the X-Road v6 SOAP profile. 
Gateway can produce flat XML elements (all children on same hierarchy level) from HTTP GET request,
non-flat XML elements from HTTP POST requests containing JSON data,
or write HTTP POST request body as-is into a SOAP attachment.

For example this GET-request:

```
[GET] http://www.example.com/rest-adapter-service/Consumer/www.restservice.com/id?param1=value1&param2=value2
```

produces this request element (inside the X-Road SOAP envelope)

```
<request>
    <resourceId>id</resourceId>
    <param1>value1</param1>
    <param2>value2</param2>
</request>
```

If HTTP request (POST / PUT / DELETE) contains a request body, there are two options:

1. the body is passed unmodified as a SOAP attachment
2. the body is converted to XML

The choice depends on the `convertPost` configuration parameter.

##### Sending the body as a SOAP attachment

the body is passed as a SOAP attachment using the format in which it is received from the client. No conversions are applied to the request body, which means that the client must send the body in the format required by the service provider. An empty ```RESTGatewayRequestBody``` element is added to the SOAP request to indicate that the request body is passed as an attachment. For example:

```
Content-Type: application/json
[POST] http://www.example.com/rest-adapter-service/Consumer/www.restservice.com/id?param1=value1&param2=value2
Request body: {"id":1,"name":"Test name"}
```
```
.
.
.
<request>
    <resourceId>id</resourceId>
    <param1>value1</param1>
    <param2>value2</param2>
    <RESTGatewayRequestBody href="RESTGatewayRequestBody" />
</request>
.
.
.
------=_Part_1_1325547227.1416893406358
Content-Type: application/json
Content-ID: RESTGatewayRequestBody

{"id":1,"name":"Test name"}
------=_Part_1_1325547227.1416893406358--
```

##### Converting the body to XML

This JSON message in HTTP POST request body
```
{   
  "sender": "Some Name",
  "message": {
    "messageId": "12345678",
    "subject": "some text",
    "text": "more text",
    "attachments": [
      {
        "attachmentId": "100",
        "name": "attachment-1",
        "mediaType": "application/pdf"
      }
    ]
  }
}
```

would be translated into e.g.

```
<some-ns:sendMessage xmlns:some-ns="http://com.example/some-namespace">
    <some-ns:sender>Some Name</some-ns:sender>
    <some-ns:message>
        <some-ns:subject>some text</some-ns:subject>
        <some-ns:attachments>
            <some-ns:name>attachment-1</some-ns:name>
            <some-ns:mediaType>application/pdf</some-ns:mediaType>
            <some-ns:attachmentId>100</some-ns:attachmentId>
        </some-ns:attachments>
        <some-ns:messageId>12345678</some-ns:messageId>
        <some-ns:text>more text</some-ns:text>
    </some-ns:message>
</some-ns:sendMessage>
```

Request element's name `sendMessage`, namespace prefix `some-ns` and namespace URI `http://com.example/some-namespace` come from the configuration.

JSON -> XML conversion has some limitations
* Consumer Gateway only understands a single namespace for the request element and it's children. 
* Gateway can't produce XML attributes from the JSON source. 
* It's not possible to produce XML with mixed content or elements.
* Gateway produces XML with unspecified child element ordering. 

In this example, subject, attachment, messageId and text elements could appear in any order.
In short, current implementation can convert only relatively simple messages.

##### Using Consumer Gateway 

Response messages's content type is given using Accept header. Supported values are ```text/xml``` and ```application/json```. However, if Provider Gateway returns the response as SOAP attachment, the value of HTTP Accept header is ignored and the response is returned in its original format.

Consumer Gateway receives HTTP GET, POST, PUT and DELETE request from information system, converts the request to SOAP message, sends the SOAP message to the Security Server, receives the SOAP response from the security server, converts the response according to the value of the request's Accept header and returns the response to the information system. User id and message id that are required by X-Road can passed via HTTP headers (X-XRd-UserId, X-XRd-MessageId) or URL parameters. If X-XRd-UserId header is missing from the request, "anonymous" is set as user id. If X-XRd-MessageId is missing from the request, unique id is generated automatically. Both headers are included in the response message.

Browser-based access makes it possible to access services that are not configured in Consumer Gateway. The identifier of the service to be called is given as resource path. X-Road message headers and other request parameters are defined as URL parameters. It's enough to know that a service with the given identifier exists in X-Road and the X-Road client identifier that's used for making the service call is authorized to call the service. However, reformatting of resource links doesn't work when using browser-based access. For example:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1/49?X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

#### Provider Gateway

Provider Gateway accepts SOAP messages following the X-Road v6 SOAP profile and translates them to REST service requests. XML request parameters are translated to REST service request URI. For example:

```
<request>
    <resourceId>id</resourceId>
    <param1>value1</param1>
    <param2>value2</param2>
</request>
```
```
[GET] http://www.restservice.com/id?param1=value1&param2=value2
```

If HTTP request body is required in the request that is sent to the service provider, the request body must be passed as a SOAP attachment. In this case SOAP request must contain an empty ```RESTGatewayRequestBody``` element, based on which Provider Gateway reads the first attachment and uses it as a HTTP request body when sending a request to a service provider. The response received from the service provider is wrapped in SOAP body or SOAP attachment according to Provider Gateway's configuration. For example:

```
.
.
.
<request>
    <resourceId>id</resourceId>
    <param1>value1</param1>
    <param2>value2</param2>
    <RESTGatewayRequestBody href="RESTGatewayRequestBody" />
</request>
.
.
.
------=_Part_1_1325547227.1416893406358
Content-Type: application/json
Content-ID: RESTGatewayRequestBody

{"id":1,"name":"Test name"}
------=_Part_1_1325547227.1416893406358--
```

```
Content-Type: application/json
[POST] http://www.restservice.com/id?param1=value1&param2=value2
Request body: {"id":1,"name":"Test name"}
```

Provider Gateway receives SOAP request from Security Server, translates the request to REST service request, sends the request to a REST service, converts the response to XML (if needed), wraps the response in SOAP message and returns the SOAP response to the security server. X-Road SOAP-headers are passed via HTTP headers (X-XRd-Client, X-XRd-Service, X-XRd-UserId, X-XRd-MessageId).

### Installation

See [README](../README.md) for description of installation options.

#### Consumer Gateway

```
http://localhost:8080/rest-adapter-service/Consumer
```

#### Provider Gateway

```
http://localhost:8080/rest-adapter-service/Provider
```

The WSDL description is accessible at:

```
http://localhost:8080/rest-adapter-service/Provider?wsdl
```
### Consumer Gateway Configuration

General settings are configured through ```consumer-gateway.properties``` configuration file. 
All the general properties are mandatory.

<table>
          <tbody>
            <tr>
              <th>Property</th>
              <th>Required</th>
              <th>Default</th>
              <th>Description</th>
            </tr>
            <tr>
              <td>id.client</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Identifier of the X-Road client that initiates the service call: instance.memberClass.memberId.subsystem <br />Can be overridden for each service.</td>
            </tr>
            <tr>
              <td>ss.url</td>
              <td>&#42;</td>
              <td>-</td>
              <td>URL or IP address of the security server. The URL of Provider Gateway can be used for testing purposes.</td>
            </tr>
			<tr>
              <td>namespace.deserialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace that's used for serializing incoming SOAP responses. Can be overridden for each service.</td>
            </tr>
            <tr>
              <td>namespace.serialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace that's used for serializing outgoing SOAP requests. Can be overridden for each service.</td>
            </tr>
            <tr>
              <td>namespace.prefix.serialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace prefix that's used for serializing outgoing SOAP requests. Can be overridden for each service.</td>
            </tr>
            <tr>
              <td>serviceCallsByXRdServiceId.enabled</td>
              <td></td>
              <td>false</td>
              <td>Boolean value that defines if browser-based access that makes it's possible to call unconfigured services using their X-Road service identifier is enabled/disabled.</td>
            </tr>
			<tr>
              <td>wrappers</td>
              <td></td>
              <td>true</td>
              <td>Use request/response -tags in SOAP message bodies.</td>
            </tr>
			<tr>
              <td>keyLength</td>
              <td></td>
              <td>128</td>
              <td>Key length (in bits) of symmetric key. Default is 128 bits. NB! Longer key requires [installing](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) unlimited key file.</td>
            </tr>
			<tr>
              <td>publicKeyFile</td>
              <td></td>
              <td>-</td>
              <td>Absolute path of the trust store file where public keys are stored.</td>
            </tr>
			<tr>
              <td>publicKeyFilePassword</td>
              <td></td>
              <td>-</td>
              <td>Password of the trust store file.</td>
            </tr>
			<tr>
              <td>privateKeyFile</td>
              <td></td>
              <td>-</td>
              <td>Absolute path of the key store file where the private key is stored.</td>
            </tr>
			<tr>
              <td>privateKeyFilePassword</td>
              <td></td>
              <td>-</td>
              <td>Password of the key store file.</td>
            </tr>
			<tr>
              <td>privateKeyAlias</td>
              <td></td>
              <td>-</td>
              <td>Alias of the private key.</td>
            </tr>
			<tr>
              <td>privateKeyPassword</td>
              <td></td>
              <td>-</td>
              <td>Password of the private key.</td>
            </tr>				
			<tr>
              <td>convertPost</td>
              <td></td>
              <td>false</td>
              <td>If true, POST requests are converted from JSON to XML and sent in the SOAP request inline. If false, POST requests are sent as SOAP attachments.</td>
            </tr>
	</tbody>
</table>

Individual services are configured through ```consumers.properties``` configuration file. Each service has 8 properties of which 3 are mandatory. Each property must be prefixed with the number of the service, e.g. ```0.id```, ```0.path```, ```0.verb```. The numbering starts from zero.

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
              <td>Identifier of the X-Road service that's called : instance.memberClass.memberId.subsystem.service.version</td>
            </tr>
            <tr>
              <td>path</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Path is the part after Consumer Gateway base URL that identifies a single service. Path may contain a resource id's that's marked with {resourceId}. If the service response contains links to other resources it's recommended that the path matches with the real URL of the service.</td>
            </tr>
            <tr>
              <td>verb</td>
              <td>&#42;</td>
              <td>-</td>
              <td>HTTP verb that is supported by the service. The service identifier is a combination of path and verb properties, so the same path can be used multiple times with different verbs.</td>
            </tr>
            <tr>
              <td>id.client</td>
              <td></td>
              <td>-</td>
              <td>Identifier of the X-Road client that initiates the service call: instance.memberClass.memberId.subsystem <br />If not defined, default value from consumser-gateway.propertiesis used.</td>
            </tr>
            <tr>
              <td>response.modurl</td>
              <td></td>
              <td>false</td>
              <td>Boolean value that indicates if Consumer Gateway should try to reformat links to other resources found in the response message. It's recommended to keep this property as false, if the response message doesn't contain links to other resources.</td>
            </tr>
            <tr>
              <td>namespace.deserialize</td>
              <td></td>
              <td>-</td>
              <td>Namespace that's used for deserializing incoming SOAP responses. If not defined, default value from consumser-gateway.properties is used.</td>
            </tr>
            <tr>
              <td>namespace.serialize</td>
              <td></td>
              <td>-</td>
              <td>Namespace that's used for serializing outgoing SOAP requests. If not defined, default value from consumser-gateway.properties is used.</td>
            </tr>
            <tr>
              <td>namespace.prefix.serialize</td>
              <td></td>
              <td>-</td>
              <td>Namespace prefix that's used for serializing outgoing SOAP requests. If not defined, default value from consumser-gateway.properties is used.</td>
            </tr>
			<tr>
              <td>wrappers</td>
              <td></td>
              <td>true</td>
              <td>Use request/response -tags in SOAP message bodies. If defined, also overrides setting from consumer-gateway.properties.</td>
            </tr>			
            <tr>
              <td>request.encrypted</td>
              <td></td>
              <td>false</td>
              <td>If set to true, request is encrypted. If value is true, all the settings related to encryption must be defined in consumer-gateway.properties file.</td>
            </tr>
            <tr>
              <td>response.encrypted</td>
              <td></td>
              <td>false</td>
              <td>If set to true, expects response to be encrypted. If value is true, all the settings related to encryption must be defined in consumer-gateway.properties file.</td>
            </tr>			
			<tr>
              <td>convertPost</td>
              <td></td>
              <td>false</td>
              <td>If true, POST requests are converted from JSON to XML and sent in the SOAP request inline. If false, POST requests are sent as SOAP attachments. If defined, also overrides setting from consumer-gateway.properties.</td>
            </tr>
</tbody>
</table>

If the response message contains links to other resources, the links are reformatted to point the consumer gateway. However, only links which beginning matches with the resource path used in Consumer Gateway are reformatted. For example:

Resource path:
```
/avoindata.prh.fi/opendata/bis/v1/
```

Full URL of the resource path on Consumer Gateway:
```
http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/
```

Link to another resource:
```
http://avoindata.prh.fi/opendata/bis/v1/2659636-7
```

Reformatted link:
```
http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2659636-7
```

This link wouldn't be reformatted as its beginning doesn't match with the resource path:

```
http://anotherapi.prh.fi/opendata/bis/v1/2659636-7
```

### Provider Gateway Configuration

General settings are configured through ```provider-gateway.properties``` configuration file. All the general properties are mandatory.

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
              <td>Path or filename of the WSDL file. 
              File reading is first attempted from WEB-INF/classes directory 
              (either inside packaged war, or in exploded war directory),
              and then from filesystem using the provided filename or path.
              </td>
            </tr>
            <tr>
              <td>namespace.deserialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace that's used for deserializing incoming SOAP requests. Can be overridden for each service.</td>
            </tr>
            <tr>
              <td>namespace.serialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace that's used for serializing outgoing SOAP responses. Can be overridden for each service.</td>
            </tr>
            <tr>
              <td>namespace.prefix.serialize</td>
              <td>&#42;</td>
              <td>-</td>
              <td>Namespace prefix that's used for serializing outgoing SOAP responses. Can be overridden for each service.</td>
            </tr>
			<tr>
              <td>wrappers</td>
              <td></td>
              <td>true</td>
              <td>Expect request/response -tags in SOAP message bodies.</td>
            </tr>
			<tr>
              <td>keyLength</td>
              <td></td>
              <td>128</td>
              <td>Key length (in bits) of symmetric key. Default is 128 bits. NB! Longer key requires [installing](http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html) unlimited key file.</td>
            </tr>
			<tr>
              <td>publicKeyFile</td>
              <td></td>
              <td>-</td>
              <td>Absolute path of the trust store file where public keys are stored.</td>
            </tr>
			<tr>
              <td>publicKeyFilePassword</td>
              <td></td>
              <td>-</td>
              <td>Password of the trust store file.</td>
            </tr>
			<tr>
              <td>privateKeyFile</td>
              <td></td>
              <td>-</td>
              <td>Absolute path of the key store file where the private key is stored.</td>
            </tr>
			<tr>
              <td>privateKeyFilePassword</td>
              <td></td>
              <td>-</td>
              <td>Password of the key store file.</td>
            </tr>
			<tr>
              <td>privateKeyAlias</td>
              <td></td>
              <td>-</td>
              <td>Alias of the private key.</td>
            </tr>
			<tr>
              <td>privateKeyPassword</td>
              <td></td>
              <td>-</td>
              <td>Password of the private key-</td>
            </tr>				
</tbody>
</table>

REST services are configured through ```providers.properties``` configuration file. Each service has 10 properties of which 2 are mandatory. Each property must be prefixed with the number of the service, e.g. ```0.id```, ```0.url```. The numbering starts from zero.

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
			<tr>
              <td>wrappers</td>
              <td></td>
              <td>true</td>
              <td>Expect request/response -tags in SOAP message bodies. If defined, also overrides setting from provider-gateway.properties.</td>
            </tr>
			<tr>
              <td>reqParamNameFilterCondition</td>
              <td></td>
              <td>-</td>
              <td>Request parameter name filter condition that's used for modifying request parameter names. Filtering is done using regex.</td>
            </tr>
			<tr>
              <td>reqParamNameFilterOperation</td>
              <td></td>
              <td>-</td>
              <td>Request parameter name filter operation that's used for modifying request parameter names. Operation is executed if and only if request parameter name matches the condition defined by reqParamNameFilterCondition.</td>
            </tr>
			<tr>
              <td>reqParamValueFilterCondition</td>
              <td></td>
              <td>-</td>
              <td>Request parameter value filter condition that's used for modifying request parameter values. Filtering is done using regex.</td>
            </tr>
			<tr>
              <td>reqParamValueFilterOperation</td>
              <td></td>
              <td>-</td>
              <td>Request parameter value filter operation that's used for modifying request parameter values. Operation is executed if and only if request parameter value matches the condition defined by reqParamValueFilterCondition.</td>
            </tr>
            <tr>
              <td>request.encrypted</td>
              <td></td>
              <td>false</td>
              <td>If set to true, expects request to be encrypted. If value is true, all the settings related to encryption must be defined in provider-gateway.properties file.</td>
            </tr>
            <tr>
              <td>response.encrypted</td>
              <td></td>
              <td>false</td>
              <td>If set to true, response is encrypted. If value is true, all the settings related to encryption must be defined in provider-gateway.properties file.</td>
            </tr>				
</tbody>
</table>

### Usage

Rest Adapter Service is shipped with configuration that includes 6 ready-to-use REST services. By default Consumer Gateway is configured to call Provider Gateway directly, and both Gateways have the same services configured ([diagram](https://raw.githubusercontent.com/educloudalliance/xroad-rest-gateway/master/images/default_configuration-rest-gateway-0.0.4.png)). In this way it's possible to test both Gateways without access to X-Road. In addition, it's possible to call Provider Gateway directly using SOAP without Consumer Gateway in the middle ([diagram](https://raw.githubusercontent.com/educloudalliance/xroad-rest-gateway/master/images/provider_call-rest-gateway-0.0.4.png)).

The preconfigured Consumer Gateway services must be called using HTTP GET and Accept header can be set to ```text/xml``` or ```application/json```. Provider Gateway services must be called using HTTP POST and Content-Type must be set to ```text/xml```. [REST Client plugin](https://addons.mozilla.org/fi/firefox/addon/restclient/) for Firefox can be used for testing purposes.

#### REST API for City of Helsinki Service Map - List of Organizations

API documentation: www.hel.fi/palvelukarttaws/restpages/ver4.html

##### Consumer Gateway

Configuration:
```
0.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1
0.path=/www.hel.fi/palvelukarttaws/rest/v4/organization/
0.verb=get
```

Service request:

* Method: ```GET```
* Accept: ```text/xml``` or ```application/json```
* X-XRd-MessageId: ```1```
* X-XRd-UserId: ```test```

URL:

```
http://localhost:8080/rest-adapter-service/Consumer/www.hel.fi/palvelukarttaws/rest/v4/organization/
```

Browser-based access:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1/?X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

Consumer Gateway response:

```
[
  {
    "name_fi": "Senioriliikunta - PKS",
    "organization_type": "UNKNOWN",
    "id": "0154f8e4-443c-4d0c-821a-c3271a47a455",
    "oid": "",
    "business_id": "0000004-3"
  },
  {
    "www_en": "https://www.visitkirkkonummi.fi/en/visitkirkkonummi/",
    "municipality_code": 257,
    "address_postal_full_fi": "PL 20, 02401 Kirkkonummi",
    "address_postal_full_sv": "PB 20, 02401 Kyrkslätt",
    "street_address_en": "Ervastintie 2",
    "address_city_fi": "Kirkkonummi",
    "address_city_sv": "Kyrkslätt",
    "oid": "",
    "street_address_fi": "Ervastintie 2",
    "street_address_sv": "Ervastvägen 2",
    "data_source_url": "www.kirkkonummi.fi",
    "address_postal_full_en": "P.O.Box 20, 02401 Kirkkonummi",
    "address_city_en": "Kirkkonummi",
    "name_fi": "Kirkkonummi",
    "name_sv": "Kyrkslätt",
    "phone": "+358 40 836 7769",
    "organization_type": "MUNICIPALITY",
    "id": "015fd5cd-b280-4d24-a5b4-0ba6ecb4c8a4",
    "business_id": "0203107-0",
    "email": "kirjaamo@kirkkonummi.fi",
    "www_sv": "https://www.kyrkslatt.fi/",
    "name_en": "Kirkkonummi"
  },
  {
    "name_fi": "Suomen Kosmetologien Yhdistyksen Opiston Säätiö sr",
    "organization_type": "FOUNDATION",
    "id": "063c6150-ccc7-4886-b44b-ecee7670d064",
    "oid": "1013-SKY",
    "business_id": "2070757-2"
  },
  {
    "address_postal_full_fi": "PL 65, 00531 HELSINKI",
    "street_address_en": "Siltasaarenkatu 3-5",
    "address_city_fi": "HELSINKI",
    "address_city_sv": "HELSINGFORS",
    "oid": "xxxx",
    "street_address_fi": "Siltasaarenkatu 3-5",
    "street_address_sv": "Broholmsgatan 3-5",
    "data_source_url": "www.akt.fi",
    "address_city_en": "HELSINKI",
    "name_fi": "Auto- ja Kuljetusalan Työntekijäliitto AKT ry",
    "name_sv": "Bil- och Transportbranschens Arbetarförbund AKT",
    "phone": "+358 9 613 111",
    "organization_type": "FOUNDATION",
    "id": "085f3a8d-c811-4d69-bbbf-a83f9661d756",
    "business_id": "0571068-5",
    "www_sv": "https://www.akt.fi/pa-svenska/"
  },
  {
    "abbr_fi": "luvn",
    "abbr_sv": "luvn",
    "www_en": "https://www.luvn.fi/en",
    "abbr_en": "luvn",
    "oid": "",
    "data_source_url": "www.luvn.fi",
    "name_fi": "Länsi-Uudenmaan hyvinvointialue",
    "name_sv": "Västra Nylands välfärdsområde",
    "organization_type": "WELLBEING_AREA",
    "id": "0c8e4f99-3d52-47b9-84df-395716bd8b11",
    "business_id": "3221347-3",
    "email": "kirjaamo@luvn.fi",
    "www_sv": "https://www.luvn.fi/sv",
    "name_en": "Western Uusimaa Wellbeing Services County"
  },
  {
    "data_source_url": "www.vihti.fi",
    "name_fi": "Vihti",
    "municipality_code": 927,
    "organization_type": "MUNICIPALITY",
    "id": "12378db7-a57e-400a-a6e4-5e99f70c0eb8",
    "oid": "",
    "business_id": "0131905-6"
  },
  {
    "street_address_fi": "Hallituskatu 1",
    "data_source_url": "www.finlit.fi",
    "www_en": "https://www.finlit.fi/en",
    "name_fi": "Suomalaisen Kirjallisuuden Seura ry",
    "phone": "+358 20 113 1231",
    "organization_type": "ASSOCIATION",
    "address_city_fi": "Helsinki",
    "id": "251d1de6-6183-4aae-92e3-773829cb0c04",
    "oid": "",
    "business_id": "0200103-1",
    "email": "sks@finlit.fi",
    "www_sv": "https://www.finlit.fi/sv"
  },
  
 <--many more organizations-->
 
  {
    "name_fi": "Teatteri Kultsa ry",
    "organization_type": "ASSOCIATION",
    "id": "f6566775-7492-4437-b24b-9f715ab31838",
    "oid": "1026-KULTS",
    "business_id": "0762383-3"
  }
]
```

##### Provider Gateway
Configuration:
```
0.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1
0.url=http://www.hel.fi/palvelukarttaws/rest/v4/organization/
```

Service request:

* Method: ```POST```
* Content-Type: ```text/xml```

URL:

```
http://localhost:8080/rest-adapter-service/Provider
```

```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>getOrganizationList</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
		<xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <test:getOrganizationList xmlns:test="http://x-road.global/producer">
            <test:request/>
        </test:getOrganizationList>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
API response URL: http://www.hel.fi/palvelukarttaws/rest/v4/organization/

Rest Adapter Service response:
```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>getOrganizationList</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <ts1:getOrganizationListResponse xmlns:ts1="http://x-road.global/producer">
            <ts1:array>
                <ts1:name_fi>Senioriliikunta - PKS</ts1:name_fi>
                <ts1:organization_type>UNKNOWN</ts1:organization_type>
                <ts1:id>0154f8e4-443c-4d0c-821a-c3271a47a455</ts1:id>
                <ts1:oid/>
                <ts1:business_id>0000004-3</ts1:business_id>
            </ts1:array>
            <ts1:array>
                <ts1:www_en>https://www.visitkirkkonummi.fi/en/visitkirkkonummi/</ts1:www_en>
                <ts1:municipality_code>257</ts1:municipality_code>
                <ts1:address_postal_full_fi>PL 20, 02401 Kirkkonummi</ts1:address_postal_full_fi>
                <ts1:address_postal_full_sv>PB 20, 02401 Kyrkslätt</ts1:address_postal_full_sv>
                <ts1:street_address_en>Ervastintie 2</ts1:street_address_en>
                <ts1:address_city_fi>Kirkkonummi</ts1:address_city_fi>
                <ts1:address_city_sv>Kyrkslätt</ts1:address_city_sv>
                <ts1:oid/>
                <ts1:street_address_fi>Ervastintie 2</ts1:street_address_fi>
                <ts1:street_address_sv>Ervastvägen 2</ts1:street_address_sv>
                <ts1:data_source_url>www.kirkkonummi.fi</ts1:data_source_url>
                <ts1:address_postal_full_en>P.O.Box 20, 02401 Kirkkonummi</ts1:address_postal_full_en>
                <ts1:address_city_en>Kirkkonummi</ts1:address_city_en>
                <ts1:name_fi>Kirkkonummi</ts1:name_fi>
                <ts1:name_sv>Kyrkslätt</ts1:name_sv>
                <ts1:phone>+358 40 836 7769</ts1:phone>
                <ts1:organization_type>MUNICIPALITY</ts1:organization_type>
                <ts1:id>015fd5cd-b280-4d24-a5b4-0ba6ecb4c8a4</ts1:id>
                <ts1:business_id>0203107-0</ts1:business_id>
                <ts1:email>kirjaamo@kirkkonummi.fi</ts1:email>
                <ts1:www_sv>https://www.kyrkslatt.fi/</ts1:www_sv>
                <ts1:name_en>Kirkkonummi</ts1:name_en>
            </ts1:array>
            <ts1:array>
                <ts1:name_fi>Suomen Kosmetologien Yhdistyksen Opiston Säätiö sr</ts1:name_fi>
                <ts1:organization_type>FOUNDATION</ts1:organization_type>
                <ts1:id>063c6150-ccc7-4886-b44b-ecee7670d064</ts1:id>
                <ts1:oid>1013-SKY</ts1:oid>
                <ts1:business_id>2070757-2</ts1:business_id>
            </ts1:array>
            <ts1:array>
                <ts1:address_postal_full_fi>PL 65, 00531 HELSINKI</ts1:address_postal_full_fi>
                <ts1:street_address_en>Siltasaarenkatu 3-5</ts1:street_address_en>
                <ts1:address_city_fi>HELSINKI</ts1:address_city_fi>
                <ts1:address_city_sv>HELSINGFORS</ts1:address_city_sv>
                <ts1:oid>xxxx</ts1:oid>
                <ts1:street_address_fi>Siltasaarenkatu 3-5</ts1:street_address_fi>
                <ts1:street_address_sv>Broholmsgatan 3-5</ts1:street_address_sv>
                <ts1:data_source_url>www.akt.fi</ts1:data_source_url>
                <ts1:address_city_en>HELSINKI</ts1:address_city_en>
                <ts1:name_fi>Auto- ja Kuljetusalan Työntekijäliitto AKT ry</ts1:name_fi>
                <ts1:name_sv>Bil- och Transportbranschens Arbetarförbund AKT</ts1:name_sv>
                <ts1:phone>+358 9 613 111</ts1:phone>
                <ts1:organization_type>FOUNDATION</ts1:organization_type>
                <ts1:id>085f3a8d-c811-4d69-bbbf-a83f9661d756</ts1:id>
                <ts1:business_id>0571068-5</ts1:business_id>
                <ts1:www_sv>https://www.akt.fi/pa-svenska/</ts1:www_sv>
            </ts1:array>
            <ts1:array>
                <ts1:abbr_fi>luvn</ts1:abbr_fi>
                <ts1:abbr_sv>luvn</ts1:abbr_sv>
                <ts1:www_en>https://www.luvn.fi/en</ts1:www_en>
                <ts1:abbr_en>luvn</ts1:abbr_en>
                <ts1:oid/>
                <ts1:data_source_url>www.luvn.fi</ts1:data_source_url>
                <ts1:name_fi>Länsi-Uudenmaan hyvinvointialue</ts1:name_fi>
                <ts1:name_sv>Västra Nylands välfärdsområde</ts1:name_sv>
                <ts1:organization_type>WELLBEING_AREA</ts1:organization_type>
                <ts1:id>0c8e4f99-3d52-47b9-84df-395716bd8b11</ts1:id>
                <ts1:business_id>3221347-3</ts1:business_id>
                <ts1:email>kirjaamo@luvn.fi</ts1:email>
                <ts1:www_sv>https://www.luvn.fi/sv</ts1:www_sv>
                <ts1:name_en>Western Uusimaa Wellbeing Services County</ts1:name_en>
            </ts1:array>
            <ts1:array>
                <ts1:data_source_url>www.vihti.fi</ts1:data_source_url>
                <ts1:name_fi>Vihti</ts1:name_fi>
                <ts1:municipality_code>927</ts1:municipality_code>
                <ts1:organization_type>MUNICIPALITY</ts1:organization_type>
                <ts1:id>12378db7-a57e-400a-a6e4-5e99f70c0eb8</ts1:id>
                <ts1:oid/>
                <ts1:business_id>0131905-6</ts1:business_id>
            </ts1:array>
            <ts1:array>
                <ts1:street_address_fi>Hallituskatu 1</ts1:street_address_fi>
                <ts1:data_source_url>www.finlit.fi</ts1:data_source_url>
                <ts1:www_en>https://www.finlit.fi/en</ts1:www_en>
                <ts1:name_fi>Suomalaisen Kirjallisuuden Seura ry</ts1:name_fi>
                <ts1:phone>+358 20 113 1231</ts1:phone>
                <ts1:organization_type>ASSOCIATION</ts1:organization_type>
                <ts1:address_city_fi>Helsinki</ts1:address_city_fi>
                <ts1:id>251d1de6-6183-4aae-92e3-773829cb0c04</ts1:id>
                <ts1:oid/>
                <ts1:business_id>0200103-1</ts1:business_id>
                <ts1:email>sks@finlit.fi</ts1:email>
                <ts1:www_sv>https://www.finlit.fi/sv</ts1:www_sv>
            </ts1:array>
            
            <---many more organizations-->
            
            <ts1:array>
                <ts1:name_fi>Teatteri Kultsa ry</ts1:name_fi>
                <ts1:organization_type>ASSOCIATION</ts1:organization_type>
                <ts1:id>f6566775-7492-4437-b24b-9f715ab31838</ts1:id>
                <ts1:oid>1026-KULTS</ts1:oid>
                <ts1:business_id>0762383-3</ts1:business_id>
            </ts1:array>
        </ts1:getOrganizationListResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

#### REST API for City of Helsinki Service Map - Department

API documentation: https://www.hel.fi/palvelukarttaws/restpages/ver4.html

##### Consumer Gateway

Configuration:
```
1.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganization.v1
1.path=/www.hel.fi/palvelukarttaws/rest/v4/department/{resourceId}
1.verb=get
```

Service request:

* Method: ```GET```
* Accept: ```text/xml``` or ```application/json```
* X-XRd-MessageId: ```1```
* X-XRd-UserId: ```test```

URL:

```
https://www.hel.fi/palvelukarttaws/rest/v4/department/cc70d1d8-3ca7-416a-9ea7-27e6b7ce58a8
```

Browser-based access:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.getDepartment.v1/cc70d1d8-3ca7-416a-9ea7-27e6b7ce58a8?X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

Consumer Gateway response:

```
{
  "abbr_fi": "Kasko",
  "www_en": "https://www.hel.fi/en/decision-making/city-organization/divisions/education-division",
  "municipality_code": 91,
  "oid": "",
  "name_fi": "Kasvatuksen ja koulutuksen toimiala",
  "name_sv": "Fostrans- och utbildningssektorn",
  "phone": "+358 9 310 8600",
  "parent_id": "83e74666-0836-4c1d-948a-4b34a8b90301",
  "org_id": "83e74666-0836-4c1d-948a-4b34a8b90301",
  "organization_type": "MUNICIPALITY",
  "id": "cc70d1d8-3ca7-416a-9ea7-27e6b7ce58a8",
  "business_id": "0201256-6",
  "email": "neuvonta.opetusvirasto@hel.fi",
  "www_fi": "https://www.hel.fi/fi/paatoksenteko-ja-hallinto/kaupungin-organisaatio/toimialat/kasvatuksen-ja-koulutuksen-toimiala",
  "www_sv": "https://www.hel.fi/sv/beslutsfattande-och-forvaltning/stadens-organisation/sektorer/fostrans-och-utbildningssektorn",
  "hierarchy_level": 1,
  "name_en": "Education Division"
}
```

##### Provider Gateway
Configuration:
```
1.id=FI_PILOT.GOV.1019125-0.Demo2Service.getDepartment.v1
1.url=https://www.hel.fi/palvelukarttaws/rest/v4/department/
```
Service request:

* Method: ```POST```
* Content-Type: ```text/xml```

URL:

```
http://localhost:8080/rest-adapter-service/Provider
```

```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>getDepartment</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
		<xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <test:getDepartment xmlns:test="http://x-road.global/producer">
            <test:request>
                <resourceId>cc70d1d8-3ca7-416a-9ea7-27e6b7ce58a8</resourceId>
            </test:request>
        </test:getDepartment>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

API response URL: https://www.hel.fi/palvelukarttaws/rest/v4/department/cc70d1d8-3ca7-416a-9ea7-27e6b7ce58a8

Rest Adapter Service response:
```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>getDepartment</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <ts1:getDepartmentResponse xmlns:ts1="http://x-road.global/producer">
            <ts1:abbr_fi>Kasko</ts1:abbr_fi>
            <ts1:www_en>https://www.hel.fi/en/decision-making/city-organization/divisions/education-division</ts1:www_en>
            <ts1:municipality_code>91</ts1:municipality_code>
            <ts1:oid/>
            <ts1:name_fi>Kasvatuksen ja koulutuksen toimiala</ts1:name_fi>
            <ts1:name_sv>Fostrans- och utbildningssektorn</ts1:name_sv>
            <ts1:phone>+358 9 310 8600</ts1:phone>
            <ts1:parent_id>83e74666-0836-4c1d-948a-4b34a8b90301</ts1:parent_id>
            <ts1:org_id>83e74666-0836-4c1d-948a-4b34a8b90301</ts1:org_id>
            <ts1:organization_type>MUNICIPALITY</ts1:organization_type>
            <ts1:id>cc70d1d8-3ca7-416a-9ea7-27e6b7ce58a8</ts1:id>
            <ts1:business_id>0201256-6</ts1:business_id>
            <ts1:email>neuvonta.opetusvirasto@hel.fi</ts1:email>
            <ts1:www_fi>https://www.hel.fi/fi/paatoksenteko-ja-hallinto/kaupungin-organisaatio/toimialat/kasvatuksen-ja-koulutuksen-toimiala</ts1:www_fi>
            <ts1:www_sv>https://www.hel.fi/sv/beslutsfattande-och-forvaltning/stadens-organisation/sektorer/fostrans-och-utbildningssektorn</ts1:www_sv>
            <ts1:hierarchy_level>1</ts1:hierarchy_level>
            <ts1:name_en>Education Division</ts1:name_en>
        </ts1:getDepartmentResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

#### Library Directory

API documentation (in Finnish): https://api.kirjastot.fi/

##### Consumer Gateway

Configuration:
```
2.id=FI_PILOT.GOV.1019125-0.Demo2Service.getLibrary.v1
2.path=/api.kirjastot.fi/v3/organisation/
2.verb=get
```

Service request:

* Method: ```GET```
* Accept: ```text/xml``` or ```application/json```
* X-XRd-MessageId: ```1```
* X-XRd-UserId: ```test```

URL:
```
http://localhost:8080/rest-adapter-service/Consumer/api.kirjastot.fi/v3/organisation/?name=kallio&city.name=helsinki
```

Browser-based access:
```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.getLibrary.v1/?name=kallio&city.name=helsinki&X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

Consumer Gateway response:

```
{
  "result": {
    "total": 1,
    "count": 1,
    "items": {
      "organisation": {
        "parent": 84846,
        "web_library": "",
        "address": {
          "area": {
            "multilang": true,
            "value": [
              {
                "lang": "se"
              },
              {
                "lang": "sv"
              },
              {
                "lang": "fi"
              },
              {
                "lang": "ru",
                "content": "Kallio"
              },
              {
                "lang": "en"
              }
            ]
          },
          "zipcode": "00530",
          "box_number": "",
          "city": {
            "multilang": true,
            "value": [
              {
                "lang": "se",
                "content": "Helsinki"
              },
              {
                "lang": "sv",
                "content": "Helsingfors"
              },
              {
                "lang": "fi",
                "content": "Helsinki"
              },
              {
                "lang": "ru",
                "content": "Helsinki"
              },
              {
                "lang": "en",
                "content": "Helsinki"
              }
            ]
          },
          "street": {
            "multilang": true,
            "value": [
              {
                "lang": "se",
                "content": "Viides linja 11"
              },
              {
                "lang": "sv",
                "content": "Femte linjen 11"
              },
              {
                "lang": "fi",
                "content": "Viides linja 11"
              },
              {
                "lang": "ru",
                "content": "Viides linja 11"
              },
              {
                "lang": "en",
                "content": "Viides linja 11"
              }
            ]
          },
          "coordinates": {
            "lon": 24.95355311,
            "lat": 60.18372258
          },
          "info": {
            "multilang": true,
            "value": [
              {
                "lang": "se"
              },
              {
                "lang": "sv"
              },
              {
                "lang": "fi"
              },
              {
                "lang": "ru"
              },
              {
                "lang": "en"
              }
            ]
          }
        },
        "city": 15885,
        "consortium": 2093,
        "type": "library",
        "provincial_library": 396,
        "branch_type": "library",
        "name": {
          "multilang": true,
          "value": [
            {
              "lang": "se",
              "content": "Kallion kirjasto"
            },
            {
              "lang": "sv",
              "content": "Berghälls bibliotek"
            },
            {
              "lang": "fi",
              "content": "Kallion kirjasto"
            },
            {
              "lang": "ru",
              "content": "Библиотека Каллио"
            },
            {
              "lang": "en",
              "content": "Kallio Library"
            }
          ]
        },
        "short_name": {
          "multilang": true,
          "value": [
            {
              "lang": "se",
              "content": "Kallio"
            },
            {
              "lang": "sv",
              "content": "Berghäll"
            },
            {
              "lang": "fi",
              "content": "Kallio"
            },
            {
              "lang": "ru",
              "content": "Каллио"
            },
            {
              "lang": "en",
              "content": "Kallio"
            }
          ]
        },
        "id": 84860,
        "region": 1003,
        "email": {
          "multilang": true,
          "value": [
            {
              "lang": "se",
              "content": "kallion_kirjasto@hel.fi"
            },
            {
              "lang": "sv",
              "content": "kallion_kirjasto@hel.fi"
            },
            {
              "lang": "fi",
              "content": "kallion_kirjasto@hel.fi"
            },
            {
              "lang": "ru",
              "content": "kallion_kirjasto@hel.fi"
            },
            {
              "lang": "en",
              "content": "kallion_kirjasto@hel.fi"
            }
          ]
        },
        "slug": {
          "multilang": true,
          "value": [
            {
              "lang": "se",
              "content": "kallio"
            },
            {
              "lang": "sv",
              "content": "berghalls-bibliotek"
            },
            {
              "lang": "fi",
              "content": "kallio"
            },
            {
              "lang": "ru",
              "content": "biblioteka-kallio"
            },
            {
              "lang": "en",
              "content": "kallio-library"
            }
          ]
        },
        "homepage": {
          "multilang": true,
          "value": [
            {
              "lang": "se",
              "content": "http://www.helmet.fi/kallionkirjasto"
            },
            {
              "lang": "sv",
              "content": "http://www.helmet.fi/berghallsbibliotek"
            },
            {
              "lang": "fi",
              "content": "http://www.helmet.fi/kallionkirjasto"
            },
            {
              "lang": "ru",
              "content": "http://www.facebook.com/kirjastokallio"
            },
            {
              "lang": "en",
              "content": "http://www.helmet.fi/kalliolibrary"
            }
          ]
        },
        "status": 1
      }
    }
  }
}
```

##### Provider Gateway

Configuration:
```
2.id=FI_PILOT.GOV.1019125-0.Demo2Service.getLibrary.v1
2.url=https://api.kirjastot.fi/v3/organisation?format=xml
```
Service request:

* Method: ```POST```
* Content-Type: ```text/xml```

URL:

```
http://localhost:8080/rest-adapter-service/Provider
```

```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>getLibrary</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <test:getLibrary xmlns:test="http://x-road.global/producer">
            <test:request>
                <test:name>Kallio</test:name>
                <test:city.name>Helsinki</test:city.name>
            </test:request>
        </test:getLibrary>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

API response URL: https://api.kirjastot.fi/v3/organisation?name=pasila&city.name=helsinki&format=xml

Rest Adapter Service response:
```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>getLibrary</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <ts1:getLibraryResponse xmlns:ts1="http://x-road.global/producer">
            <result count="1" total="1">
                <items>
                    <organisation>
                        <parent>84846</parent>
                        <web_library/>
                        <address>
                            <area multilang="true">
                                <value lang="se"/>
                                <value lang="sv"/>
                                <value lang="fi"/>
                                <value lang="ru">Kallio</value>
                                <value lang="en"/>
                            </area>
                            <zipcode>00530</zipcode>
                            <box_number/>
                            <city multilang="true">
                                <value lang="se">Helsinki</value>
                                <value lang="sv">Helsingfors</value>
                                <value lang="fi">Helsinki</value>
                                <value lang="ru">Helsinki</value>
                                <value lang="en">Helsinki</value>
                            </city>
                            <street multilang="true">
                                <value lang="se">Viides linja 11</value>
                                <value lang="sv">Femte linjen 11</value>
                                <value lang="fi">Viides linja 11</value>
                                <value lang="ru">Viides linja 11</value>
                                <value lang="en">Viides linja 11</value>
                            </street>
                            <coordinates>
                                <lon>24.95355311</lon>
                                <lat>60.18372258</lat>
                            </coordinates>
                            <info multilang="true">
                                <value lang="se"/>
                                <value lang="sv"/>
                                <value lang="fi"/>
                                <value lang="ru"/>
                                <value lang="en"/>
                            </info>
                        </address>
                        <city>15885</city>
                        <consortium>2093</consortium>
                        <type>library</type>
                        <branch_type>library</branch_type>
                        <name multilang="true">
                            <value lang="se">Kallion kirjasto</value>
                            <value lang="sv">Berghälls bibliotek</value>
                            <value lang="fi">Kallion kirjasto</value>
                            <value lang="ru">Библиотека Каллио</value>
                            <value lang="en">Kallio Library</value>
                        </name>
                        <short_name multilang="true">
                            <value lang="se">Kallio</value>
                            <value lang="sv">Berghäll</value>
                            <value lang="fi">Kallio</value>
                            <value lang="ru">Каллио</value>
                            <value lang="en">Kallio</value>
                        </short_name>
                        <id>84860</id>
                        <email multilang="true">
                            <value lang="se">kallion_kirjasto@hel.fi</value>
                            <value lang="sv">kallion_kirjasto@hel.fi</value>
                            <value lang="fi">kallion_kirjasto@hel.fi</value>
                            <value lang="ru">kallion_kirjasto@hel.fi</value>
                            <value lang="en">kallion_kirjasto@hel.fi</value>
                        </email>
                        <slug multilang="true">
                            <value lang="se">kallio</value>
                            <value lang="sv">berghalls-bibliotek</value>
                            <value lang="fi">kallio</value>
                            <value lang="ru">biblioteka-kallio</value>
                            <value lang="en">kallio-library</value>
                        </slug>
                        <homepage multilang="true">
                            <value lang="se">http://www.helmet.fi/kallionkirjasto</value>
                            <value lang="sv">http://www.helmet.fi/berghallsbibliotek</value>
                            <value lang="fi">http://www.helmet.fi/kallionkirjasto</value>
                            <value lang="ru">http://www.facebook.com/kirjastokallio</value>
                            <value lang="en">http://www.helmet.fi/kalliolibrary</value>
                        </homepage>
                        <status>1</status>
                        <region>1003</region>
                        <provincial_library>396</provincial_library>
                    </organisation>
                </items>
            </result>
        </ts1:getLibraryResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
#### Finnish Patent and Registration Office - Business Information Search

API documentation: http://avoindata.prh.fi/ytj_en.html

##### Consumer Gateway

Configuration:
```
3.id=FI_PILOT.GOV.1019125-0.Demo2Service.searchCompany.v1
3.path=/avoindata.prh.fi/opendata-ytj-api/v3/companies
3.verb=get
3.response.modurl=true
```

Service request:

* Method: ```GET```
* Accept: ```text/xml``` or ```application/json```
* X-XRd-MessageId: ```1```
* X-XRd-UserId: ```test```

URL:
```
http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata-ytj-api/v3/companies?name=asunto&location=Turku&registrationDateStart=2015-02-28&registrationDateEnd=2016-02-28&totalResults=false&resultsFrom=0&Accept=application/json
```

Browser-based access:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.searchCompany.v1/?name=asunto&location=Turku&registrationDateStart=2015-02-28&registrationDateEnd=2016-02-28&totalResults=false&resultsFrom=0&Accept=application/json
```

Consumer Gateway response:

```
{
  "totalResults": 33,
  "companies": [
    {
      "addresses": [
        {
          "postOffices": [
            {
              "city": "LIETO",
              "languageCode": 1,
              "municipalityCode": 423
            },
            {
              "city": "LIETO",
              "languageCode": 2,
              "municipalityCode": 423
            }
          ],
          "apartmentIdSuffix": "",
          "street": "Ajurintie",
          "registrationDate": "2024-07-12",
          "buildingNumber": 3,
          "postCode": 21420,
          "source": 0,
          "type": 1,
          "entrance": "",
          "co": "",
          "apartmentNumber": ""
        },
        {
          "postOffices": [
            {
              "city": "TURKU",
              "languageCode": 1,
              "municipalityCode": 853
            },
            {
              "city": "ÅBO",
              "languageCode": 2,
              "municipalityCode": 853
            }
          ],
          "postOfficeBox": "",
          "apartmentIdSuffix": "",
          "street": "Rauhankatu",
          "registrationDate": "2024-07-12",
          "buildingNumber": "9b",
          "postCode": 20100,
          "source": 0,
          "type": 2,
          "entrance": "",
          "co": "c/o Aboa Isännöintipalvelut Oy",
          "apartmentNumber": ""
        }
      ],
      "names": {
        "name": "Asunto Oy Liedon Graniitti",
        "registrationDate": "2015-03-06",
        "source": 1,
        "type": 1,
        "version": 1
      },
      "registeredEntries": [
        {
          "endDate": "2015-03-05",
          "authority": 2,
          "registrationDate": "2015-02-24",
          "type": 0,
          "descriptions": [
            {
              "description": "Oregistrerad",
              "languageCode": 2
            },
            {
              "description": "Rekisteröimätön",
              "languageCode": 1
            },
            {
              "description": "Unregistered",
              "languageCode": 3
            }
          ],
          "register": 1
        },
        {
          "authority": 2,
          "registrationDate": "2015-03-06",
          "type": 1,
          "descriptions": [
            {
              "description": "Registered",
              "languageCode": 3
            },
            {
              "description": "Registrerad",
              "languageCode": 2
            },
            {
              "description": "Rekisterissä",
              "languageCode": 1
            }
          ],
          "register": 1
        },
        {
          "authority": 1,
          "registrationDate": "2015-03-09",
          "type": 1,
          "descriptions": [
            {
              "description": "Registered",
              "languageCode": 3
            },
            {
              "description": "Registrerad",
              "languageCode": 2
            },
            {
              "description": "Rekisterissä",
              "languageCode": 1
            }
          ],
          "register": 4
        }
      ],
      "businessId": {
        "registrationDate": "2015-02-24",
        "source": 3,
        "value": "2678126-3"
      },
      "companyForms": {
        "registrationDate": "2015-03-06",
        "source": 1,
        "type": 2,
        "descriptions": [
          {
            "description": "Asunto-osakeyhtiö",
            "languageCode": 1
          },
          {
            "description": "Bostadsaktiebolag",
            "languageCode": 2
          },
          {
            "description": "Housing corporation",
            "languageCode": 3
          }
        ],
        "version": 1
      },
      "registrationDate": "2015-03-06",
      "lastModified": "2025-05-05 15:39:54",
      "mainBusinessLine": {
        "typeCodeSet": "TOIMI3",
        "registrationDate": "2015-02-17",
        "source": 2,
        "type": 68202,
        "descriptions": [
          {
            "description": "Asuntojen ja asuinkiinteistöjen hallinta",
            "languageCode": 1
          },
          {
            "description": "Förvaltning av bostäder och bostadsfastigheter",
            "languageCode": 2
          },
          {
            "description": "Operation of dwellings and residential real estate",
            "languageCode": 3
          }
        ]
      },
      "tradeRegisterStatus": 1,
      "status": 2
    },
    
    <-- many more companies --> 
    
    {
      "addresses": [
        {
          "postOffices": [
            {
              "city": "TURKU",
              "languageCode": 1,
              "municipalityCode": 853
            },
            {
              "city": "ÅBO",
              "languageCode": 2,
              "municipalityCode": 853
            }
          ],
          "apartmentIdSuffix": "",
          "street": "Valerinkuja",
          "registrationDate": "2024-07-09",
          "buildingNumber": 7,
          "postCode": 20900,
          "source": 0,
          "type": 1,
          "entrance": "B",
          "co": "c/o Satu nSalonen",
          "apartmentNumber": 3
        },
        {
          "postOffices": [
            {
              "city": "TURKU",
              "languageCode": 1,
              "municipalityCode": 853
            },
            {
              "city": "ÅBO",
              "languageCode": 2,
              "municipalityCode": 853
            }
          ],
          "postOfficeBox": "",
          "apartmentIdSuffix": "",
          "street": "Valerinkuja",
          "registrationDate": "2024-07-09",
          "buildingNumber": 7,
          "postCode": 20900,
          "source": 0,
          "type": 2,
          "entrance": "B",
          "co": "c/o Satu Salonen",
          "apartmentNumber": 3
        }
      ],
      "names": {
        "name": "Asunto Oy Turun Kesätuuli",
        "registrationDate": "2016-01-25",
        "source": 1,
        "type": 1,
        "version": 1
      },
      "registeredEntries": [
        {
          "endDate": "2016-01-24",
          "authority": 2,
          "registrationDate": "2016-01-20",
          "type": 0,
          "descriptions": [
            {
              "description": "Oregistrerad",
              "languageCode": 2
            },
            {
              "description": "Rekisteröimätön",
              "languageCode": 1
            },
            {
              "description": "Unregistered",
              "languageCode": 3
            }
          ],
          "register": 1
        },
        {
          "authority": 2,
          "registrationDate": "2016-01-25",
          "type": 1,
          "descriptions": [
            {
              "description": "Registered",
              "languageCode": 3
            },
            {
              "description": "Registrerad",
              "languageCode": 2
            },
            {
              "description": "Rekisterissä",
              "languageCode": 1
            }
          ],
          "register": 1
        },
        {
          "authority": 1,
          "registrationDate": "2016-01-26",
          "type": 1,
          "descriptions": [
            {
              "description": "Registered",
              "languageCode": 3
            },
            {
              "description": "Registrerad",
              "languageCode": 2
            },
            {
              "description": "Rekisterissä",
              "languageCode": 1
            }
          ],
          "register": 4
        }
      ],
      "businessId": {
        "registrationDate": "2016-01-20",
        "source": 3,
        "value": "2738710-2"
      },
      "companyForms": {
        "registrationDate": "2016-01-25",
        "source": 1,
        "type": 2,
        "descriptions": [
          {
            "description": "Asunto-osakeyhtiö",
            "languageCode": 1
          },
          {
            "description": "Bostadsaktiebolag",
            "languageCode": 2
          },
          {
            "description": "Housing corporation",
            "languageCode": 3
          }
        ],
        "version": 1
      },
      "registrationDate": "2016-01-25",
      "lastModified": "2025-01-25 00:17:38",
      "mainBusinessLine": {
        "typeCodeSet": "TOIMI3",
        "registrationDate": "2015-12-14",
        "source": 2,
        "type": 68202,
        "descriptions": [
          {
            "description": "Asuntojen ja asuinkiinteistöjen hallinta",
            "languageCode": 1
          },
          {
            "description": "Förvaltning av bostäder och bostadsfastigheter",
            "languageCode": 2
          },
          {
            "description": "Operation of dwellings and residential real estate",
            "languageCode": 3
          }
        ]
      },
      "tradeRegisterStatus": 1,
      "status": 2
    }
  ]
}
```

##### Provider Gateway

Configuration:
```
3.id=FI_PILOT.GOV.1019125-0.Demo2Service.searchCompany.v1
3.url=https://avoindata.prh.fi/opendata-ytj-api/v3/companies
```
Service request:

* Method: ```POST```
* Content-Type: ```text/xml```

URL:

```
http://localhost:8080/rest-adapter-service/Provider
```

```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>searchCompany</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
		<xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <test:searchCompany xmlns:test="http://x-road.global/producer">
            <test:request>
                <totalResults>false</totalResults>
                <maxResults>10</maxResults>
                <resultsFrom>0</resultsFrom>
                <name>asunto</name>
                <location>Turku</location>
                <registrationDateStart>2015-02-28</registrationDateStart>
                 <registrationDateEnd>2016-02-28</registrationDateEnd>
            </test:request>
        </test:searchCompany>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

API results URL: https://avoindata.prh.fi/opendata-ytj-api/v3/companies?name=asunto&location=Turku&registrationDateStart=2015-02-28&registrationDateEnd=2016-02-28

Rest Adapter Service response:
```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>searchCompany</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <ts1:searchCompanyResponse xmlns:ts1="http://x-road.global/producer">
            <ts1:totalResults>33</ts1:totalResults>
            <ts1:companies>
                <ts1:addresses>
                    <ts1:postOffices>
                        <ts1:city>LIETO</ts1:city>
                        <ts1:languageCode>1</ts1:languageCode>
                        <ts1:municipalityCode>423</ts1:municipalityCode>
                    </ts1:postOffices>
                    <ts1:postOffices>
                        <ts1:city>LIETO</ts1:city>
                        <ts1:languageCode>2</ts1:languageCode>
                        <ts1:municipalityCode>423</ts1:municipalityCode>
                    </ts1:postOffices>
                    <ts1:apartmentIdSuffix/>
                    <ts1:street>Ajurintie</ts1:street>
                    <ts1:registrationDate>2024-07-12</ts1:registrationDate>
                    <ts1:buildingNumber>3</ts1:buildingNumber>
                    <ts1:postCode>21420</ts1:postCode>
                    <ts1:source>0</ts1:source>
                    <ts1:type>1</ts1:type>
                    <ts1:entrance/>
                    <ts1:co/>
                    <ts1:apartmentNumber/>
                </ts1:addresses>
                <ts1:addresses>
                    <ts1:postOffices>
                        <ts1:city>TURKU</ts1:city>
                        <ts1:languageCode>1</ts1:languageCode>
                        <ts1:municipalityCode>853</ts1:municipalityCode>
                    </ts1:postOffices>
                    <ts1:postOffices>
                        <ts1:city>ÅBO</ts1:city>
                        <ts1:languageCode>2</ts1:languageCode>
                        <ts1:municipalityCode>853</ts1:municipalityCode>
                    </ts1:postOffices>
                    <ts1:postOfficeBox/>
                    <ts1:apartmentIdSuffix/>
                    <ts1:street>Rauhankatu</ts1:street>
                    <ts1:registrationDate>2024-07-12</ts1:registrationDate>
                    <ts1:buildingNumber>9b</ts1:buildingNumber>
                    <ts1:postCode>20100</ts1:postCode>
                    <ts1:source>0</ts1:source>
                    <ts1:type>2</ts1:type>
                    <ts1:entrance/>
                    <ts1:co>c/o Aboa Isännöintipalvelut Oy</ts1:co>
                    <ts1:apartmentNumber/>
                </ts1:addresses>
                <ts1:names>
                    <ts1:name>Asunto Oy Liedon Graniitti</ts1:name>
                    <ts1:registrationDate>2015-03-06</ts1:registrationDate>
                    <ts1:source>1</ts1:source>
                    <ts1:type>1</ts1:type>
                    <ts1:version>1</ts1:version>
                </ts1:names>
                <ts1:registeredEntries>
                    <ts1:endDate>2015-03-05</ts1:endDate>
                    <ts1:authority>2</ts1:authority>
                    <ts1:registrationDate>2015-02-24</ts1:registrationDate>
                    <ts1:type>0</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Oregistrerad</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Rekisteröimätön</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Unregistered</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:register>1</ts1:register>
                </ts1:registeredEntries>
                <ts1:registeredEntries>
                    <ts1:authority>2</ts1:authority>
                    <ts1:registrationDate>2015-03-06</ts1:registrationDate>
                    <ts1:type>1</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Registered</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Registrerad</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Rekisterissä</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:register>1</ts1:register>
                </ts1:registeredEntries>
                <ts1:registeredEntries>
                    <ts1:authority>1</ts1:authority>
                    <ts1:registrationDate>2015-03-09</ts1:registrationDate>
                    <ts1:type>1</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Registered</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Registrerad</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Rekisterissä</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:register>4</ts1:register>
                </ts1:registeredEntries>
                <ts1:businessId>
                    <ts1:registrationDate>2015-02-24</ts1:registrationDate>
                    <ts1:source>3</ts1:source>
                    <ts1:value>2678126-3</ts1:value>
                </ts1:businessId>
                <ts1:companyForms>
                    <ts1:registrationDate>2015-03-06</ts1:registrationDate>
                    <ts1:source>1</ts1:source>
                    <ts1:type>2</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Asunto-osakeyhtiö</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Bostadsaktiebolag</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Housing corporation</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:version>1</ts1:version>
                </ts1:companyForms>
                <ts1:registrationDate>2015-03-06</ts1:registrationDate>
                <ts1:lastModified>2025-05-05 15:39:54</ts1:lastModified>
                <ts1:mainBusinessLine>
                    <ts1:typeCodeSet>TOIMI3</ts1:typeCodeSet>
                    <ts1:registrationDate>2015-02-17</ts1:registrationDate>
                    <ts1:source>2</ts1:source>
                    <ts1:type>68202</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Asuntojen ja asuinkiinteistöjen hallinta</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Förvaltning av bostäder och bostadsfastigheter</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Operation of dwellings and residential real estate</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                </ts1:mainBusinessLine>
                <ts1:tradeRegisterStatus>1</ts1:tradeRegisterStatus>
                <ts1:status>2</ts1:status>
            </ts1:companies>
            
            <-- many more companies -->
            
            <ts1:companies>
                <ts1:addresses>
                    <ts1:postOffices>
                        <ts1:city>TURKU</ts1:city>
                        <ts1:languageCode>1</ts1:languageCode>
                        <ts1:municipalityCode>853</ts1:municipalityCode>
                    </ts1:postOffices>
                    <ts1:postOffices>
                        <ts1:city>ÅBO</ts1:city>
                        <ts1:languageCode>2</ts1:languageCode>
                        <ts1:municipalityCode>853</ts1:municipalityCode>
                    </ts1:postOffices>
                    <ts1:apartmentIdSuffix/>
                    <ts1:street>Valerinkuja</ts1:street>
                    <ts1:registrationDate>2024-07-09</ts1:registrationDate>
                    <ts1:buildingNumber>7</ts1:buildingNumber>
                    <ts1:postCode>20900</ts1:postCode>
                    <ts1:source>0</ts1:source>
                    <ts1:type>1</ts1:type>
                    <ts1:entrance>B</ts1:entrance>
                    <ts1:co>c/o Satu nSalonen</ts1:co>
                    <ts1:apartmentNumber>3</ts1:apartmentNumber>
                </ts1:addresses>
                <ts1:addresses>
                    <ts1:postOffices>
                        <ts1:city>TURKU</ts1:city>
                        <ts1:languageCode>1</ts1:languageCode>
                        <ts1:municipalityCode>853</ts1:municipalityCode>
                    </ts1:postOffices>
                    <ts1:postOffices>
                        <ts1:city>ÅBO</ts1:city>
                        <ts1:languageCode>2</ts1:languageCode>
                        <ts1:municipalityCode>853</ts1:municipalityCode>
                    </ts1:postOffices>
                    <ts1:postOfficeBox/>
                    <ts1:apartmentIdSuffix/>
                    <ts1:street>Valerinkuja</ts1:street>
                    <ts1:registrationDate>2024-07-09</ts1:registrationDate>
                    <ts1:buildingNumber>7</ts1:buildingNumber>
                    <ts1:postCode>20900</ts1:postCode>
                    <ts1:source>0</ts1:source>
                    <ts1:type>2</ts1:type>
                    <ts1:entrance>B</ts1:entrance>
                    <ts1:co>c/o Satu Salonen</ts1:co>
                    <ts1:apartmentNumber>3</ts1:apartmentNumber>
                </ts1:addresses>
                <ts1:names>
                    <ts1:name>Asunto Oy Turun Kesätuuli</ts1:name>
                    <ts1:registrationDate>2016-01-25</ts1:registrationDate>
                    <ts1:source>1</ts1:source>
                    <ts1:type>1</ts1:type>
                    <ts1:version>1</ts1:version>
                </ts1:names>
                <ts1:registeredEntries>
                    <ts1:endDate>2016-01-24</ts1:endDate>
                    <ts1:authority>2</ts1:authority>
                    <ts1:registrationDate>2016-01-20</ts1:registrationDate>
                    <ts1:type>0</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Oregistrerad</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Rekisteröimätön</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Unregistered</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:register>1</ts1:register>
                </ts1:registeredEntries>
                <ts1:registeredEntries>
                    <ts1:authority>2</ts1:authority>
                    <ts1:registrationDate>2016-01-25</ts1:registrationDate>
                    <ts1:type>1</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Registered</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Registrerad</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Rekisterissä</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:register>1</ts1:register>
                </ts1:registeredEntries>
                <ts1:registeredEntries>
                    <ts1:authority>1</ts1:authority>
                    <ts1:registrationDate>2016-01-26</ts1:registrationDate>
                    <ts1:type>1</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Registered</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Registrerad</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Rekisterissä</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:register>4</ts1:register>
                </ts1:registeredEntries>
                <ts1:businessId>
                    <ts1:registrationDate>2016-01-20</ts1:registrationDate>
                    <ts1:source>3</ts1:source>
                    <ts1:value>2738710-2</ts1:value>
                </ts1:businessId>
                <ts1:companyForms>
                    <ts1:registrationDate>2016-01-25</ts1:registrationDate>
                    <ts1:source>1</ts1:source>
                    <ts1:type>2</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Asunto-osakeyhtiö</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Bostadsaktiebolag</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Housing corporation</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:version>1</ts1:version>
                </ts1:companyForms>
                <ts1:registrationDate>2016-01-25</ts1:registrationDate>
                <ts1:lastModified>2025-01-25 00:17:38</ts1:lastModified>
                <ts1:mainBusinessLine>
                    <ts1:typeCodeSet>TOIMI3</ts1:typeCodeSet>
                    <ts1:registrationDate>2015-12-14</ts1:registrationDate>
                    <ts1:source>2</ts1:source>
                    <ts1:type>68202</ts1:type>
                    <ts1:descriptions>
                        <ts1:description>Asuntojen ja asuinkiinteistöjen hallinta</ts1:description>
                        <ts1:languageCode>1</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Förvaltning av bostäder och bostadsfastigheter</ts1:description>
                        <ts1:languageCode>2</ts1:languageCode>
                    </ts1:descriptions>
                    <ts1:descriptions>
                        <ts1:description>Operation of dwellings and residential real estate</ts1:description>
                        <ts1:languageCode>3</ts1:languageCode>
                    </ts1:descriptions>
                </ts1:mainBusinessLine>
                <ts1:tradeRegisterStatus>1</ts1:tradeRegisterStatus>
                <ts1:status>2</ts1:status>
            </ts1:companies>
        </ts1:searchCompanyResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

#### Finto : Finnish Thesaurus and Ontology Service - Search

API documentation: http://api.finto.fi/
##### Consumer Gateway

Configuration:
```
5.id=FI_PILOT.GOV.1019125-0.Demo2Service.fintoService.v1
5.path=/api.finto.fi/rest/v1/search/
5.verb=get
```

Service request:

* Method: ```GET```
* Accept: ```text/xml``` or ```application/json```
* X-XRd-MessageId: ```1```
* X-XRd-UserId: ```test```

URL:
```
http://localhost:8080/rest-adapter-service/Consumer/api.finto.fi/rest/v1/search/?query=cat&lang=en
```

Browser-based access:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.fintoService.v1/?query=cat&lang=en&X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

Consumer  Gateway response:

```
{
  "@context": {
    "hiddenLabel": "skos:hiddenLabel",
    "prefLabel": "skos:prefLabel",
    "skos": "http://www.w3.org/2004/02/skos/core#",
    "isothes": "http://purl.org/iso25964/skos-thes#",
    "onki": "http://schema.onki.fi/onki#",
    "altLabel": "skos:altLabel",
    "type": "@type",
    "@language": "en",
    "uri": "@id",
    "results": {
      "@container": "@list",
      "@id": "onki:results"
    }
  },
  "uri": "",
  "results": [
    {
      "notation": "34B12",
      "prefLabel": "cat",
      "vocab": "ic",
      "type": "skos:Concept",
      "lang": "en",
      "uri": "http://iconclass.org/34B12"
    },
    {
      "prefLabel": "cat",
      "vocab": "afo",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/afo-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/afo/p1287"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "afo",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "geo",
      "type": "skos:Concept",
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "prefLabel": "cat",
      "vocab": "hero",
      "type": "skos:Concept",
      "lang": "en",
      "uri": "http://www.yso.fi/onto/hero/p1227"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "juho",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "jupo",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "kauno",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "keko",
      "type": "skos:Concept",
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "kito",
      "type": "skos:Concept",
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "prefLabel": "cat",
      "vocab": "koko",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept",
        "http://www.yso.fi/onto/kauno-meta/Concept",
        "http://www.yso.fi/onto/afo-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/koko/p37252"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "kto",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "kulo",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "liiko",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "muso",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "oiko",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "oma",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "pto",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "puho",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "soto",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "maotao",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "tero",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "tsr",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "exvocab": "yso",
      "prefLabel": "cat",
      "vocab": "valo",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    },
    {
      "prefLabel": "cat",
      "vocab": "yso",
      "type": [
        "skos:Concept",
        "http://www.yso.fi/onto/yso-meta/Concept"
      ],
      "lang": "en",
      "uri": "http://www.yso.fi/onto/yso/p19378"
    }
  ]
}
```

##### Provider Gateway

Configuration:
```
5.id=FI_PILOT.GOV.1019125-0.Demo2Service.fintoService.v1
5.url=http://api.finto.fi/rest/v1/search
```
Service request:

* Method: ```POST```
* Content-Type: ```text/xml```

URL:

```
http://localhost:8080/rest-adapter-service/Provider
```

```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>fintoService</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
		<xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <test:fintoService xmlns:test="http://x-road.global/producer">
            <test:request>
                <test:query>cat</test:query>
                <test:lang>en</test:lang>
            </test:request>
        </test:fintoService>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

API response URL: http://api.finto.fi/rest/v1/search?query=cat&lang=en

Rest Adapter Service response:
```
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/" xmlns:id="http://x-road.eu/xsd/identifiers" xmlns:xrd="http://x-road.eu/xsd/xroad.xsd">
    <SOAP-ENV:Header>
        <xrd:client id:objectType="SUBSYSTEM">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>0245437-2</id:memberCode>
            <id:subsystemCode>ConsumerService</id:subsystemCode>
        </xrd:client>
        <xrd:service id:objectType="SERVICE">
            <id:xRoadInstance>FI_PILOT</id:xRoadInstance>
            <id:memberClass>GOV</id:memberClass>
            <id:memberCode>1019125-0</id:memberCode>
            <id:subsystemCode>Demo2Service</id:subsystemCode>
            <id:serviceCode>fintoService</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <ts1:fintoServiceResponse xmlns:ts1="http://x-road.global/producer">
            <ts1:__at__context>
                <ts1:hiddenLabel>skos:hiddenLabel</ts1:hiddenLabel>
                <ts1:__at__language>en</ts1:__at__language>
                <ts1:prefLabel>skos:prefLabel</ts1:prefLabel>
                <ts1:skos>http://www.w3.org/2004/02/skos/core#</ts1:skos>
                <ts1:isothes>http://purl.org/iso25964/skos-thes#</ts1:isothes>
                <ts1:onki>http://schema.onki.fi/onki#</ts1:onki>
                <ts1:altLabel>skos:altLabel</ts1:altLabel>
                <ts1:type>@type</ts1:type>
                <ts1:uri>@id</ts1:uri>
                <ts1:results>
                    <ts1:__at__id>onki:results</ts1:__at__id>
                    <ts1:__at__container>@list</ts1:__at__container>
                </ts1:results>
            </ts1:__at__context>
            <ts1:uri/>
            <ts1:results>
                <ts1:notation>34B12</ts1:notation>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>ic</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://iconclass.org/34B12</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>afo</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/afo-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/afo/p1287</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>afo</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>geo</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>hero</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/hero/p1227</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>juho</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>jupo</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>kauno</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>keko</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>kito</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>koko</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/kauno-meta/Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/afo-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/koko/p37252</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>kto</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>kulo</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>liiko</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>muso</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>oiko</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>oma</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>pto</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>puho</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>soto</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>maotao</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>tero</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>tsr</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:exvocab>yso</ts1:exvocab>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>valo</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
            <ts1:results>
                <ts1:prefLabel>cat</ts1:prefLabel>
                <ts1:vocab>yso</ts1:vocab>
                <ts1:type>skos:Concept</ts1:type>
                <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                <ts1:lang>en</ts1:lang>
                <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
            </ts1:results>
        </ts1:fintoServiceResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
