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

API documentation: http://www.hel.fi/palvelukarttaws/rest/ver2_en.html

#### Consumer Gateway

Configuration:
```
0.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1
0.path=/www.hel.fi/palvelukarttaws/rest/v2/organization/
0.verb=get
```

Service request:

* Method: ```GET```
* Accept: ```text/xml``` or ```application/json```
* X-XRd-MessageId: ```1```
* X-XRd-UserId: ```test```

URL:

```
http://localhost:8080/rest-adapter-service/Consumer/www.hel.fi/palvelukarttaws/rest/v2/organization/
```

Browser-based access:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1/?X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

Consumer Gateway response:

```
[
    {
        "id": 49,
        "name_en": "City of Espoo",
        "name_sv": "Esbo stad",
        "data_source_url": "www.espoo.fi",
        "name_fi": "Espoon kaupunki"
    },
    {
        "id": 91,
        "name_en": "City of Helsinki",
        "name_sv": "Helsingfors stad",
        "data_source_url": "www.hel.fi",
        "name_fi": "Helsingin kaupunki"
    },
    {
        "id": 92,
        "name_en": "City of Vantaa",
        "name_sv": "Vanda stad",
        "data_source_url": "www.vantaa.fi",
        "name_fi": "Vantaan kaupunki"
    },
    {
        "id": 235,
        "name_en": "City of Kauniainen",
        "name_sv": "Grankulla stad",
        "data_source_url": "www.kauniainen.fi",
        "name_fi": "Kauniaisten kaupunki"
    },
    {
        "id": 1000,
        "name_en": "State IT Service Centre, Suomi.fi editorial team",
        "name_sv": "Statens IT-servicecentral, Suomi.fi-redaktionen",
        "data_source_url": "www.suomi.fi",
        "name_fi": "Valtion IT-palvelukeskus, Suomi.fi-toimitus"
    },
    {
        "id": 1001,
        "name_en": "HUS Hospital District",
        "name_sv": "Samkommunen HNS",
        "data_source_url": "www.hus.fi",
        "name_fi": "HUS-kuntayhtymä"
    },
    {
        "id": 1002,
        "name_en": "Helsinki Marketing Ltd",
        "name_sv": "Helsinki Marketing Ltd",
        "data_source_url": "www.visithelsinki.fi",
        "name_fi": "Helsingin markkinointi Oy"
    },
    {
        "id": 1003,
        "name_en": "Ministry of Justice, Election Unit",
        "name_sv": "Justitieministeriet, den högsta valmyndigheten",
        "data_source_url": "www.vaalit.fi",
        "name_fi": "Oikeusministeriö, ylin vaaliviranomainen"
    },
    {
        "id": 1004,
        "name_en": "Helsinki Region Environmental Services Authority HSY",
        "name_sv": "Helsingforsregionens miljötjänster HRM",
        "data_source_url": "www.hsy.fi",
        "name_fi": "Helsingin seudun ympäristöpalvelut HSY"
    },
    {
        "id": 1005,
        "name_en": "Service Map editorial team",
        "name_sv": "Servicekartans redaktion",
        "data_source_url": "www.hel.fi/palvelukartta",
        "name_fi": "Palvelukartan toimitus"
    },
    {
        "id": 1007,
        "name_en": "JLY - Finnish Solid Waste Association",
        "name_sv": "JLY - Avfallsverksföreningen rf",
        "data_source_url": "www.jly.fi",
        "name_fi": "JLY Jätelaitosyhdistys ry"
    },
    {
        "id": 1008,
        "name_en": "The Norwegian Electric Vehicle Association",
        "name_sv": "Norsk Elbilforening",
        "data_source_url": "NOBIL.no",
        "name_fi": "Norsk Elbilforening, sähköautojen latauspisteet"
    },
    {
        "id": 1009,
        "name_en": "External service point register user society",
        "name_sv": "Gemenskapen bakom externa serviceregister",
        "data_source_url": "asiointi.hel.fi/tprulkoinen",
        "name_fi": "Ulkoisen toimipisterekisterin käyttäjäyhteisö"
    },
    {
        "id": 1010,
        "name_en": "University of Jyväskylä, LIPAS Liikuntapaikat.fi",
        "name_sv": "Jyväskylä universitet, LIPAS Liikuntapaikat.fi",
        "data_source_url": "www.liikuntapaikat.fi",
        "name_fi": "Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi"
    }
]
```

#### Provider Gateway
Configuration:
```
0.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1
0.url=http://www.hel.fi/palvelukarttaws/rest/v2/organization/
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
API response URL: http://www.hel.fi/palvelukarttaws/rest/v2/organization/

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
            <ts1:request/>
            <ts1:response>
                <ts1:array>
                    <ts1:id>49</ts1:id>
                    <ts1:name_en>City of Espoo</ts1:name_en>
                    <ts1:name_sv>Esbo stad</ts1:name_sv>
                    <ts1:data_source_url>www.espoo.fi</ts1:data_source_url>
                    <ts1:name_fi>Espoon kaupunki</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>91</ts1:id>
                    <ts1:name_en>City of Helsinki</ts1:name_en>
                    <ts1:name_sv>Helsingfors stad</ts1:name_sv>
                    <ts1:data_source_url>www.hel.fi</ts1:data_source_url>
                    <ts1:name_fi>Helsingin kaupunki</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>92</ts1:id>
                    <ts1:name_en>City of Vantaa</ts1:name_en>
                    <ts1:name_sv>Vanda stad</ts1:name_sv>
                    <ts1:data_source_url>www.vantaa.fi</ts1:data_source_url>
                    <ts1:name_fi>Vantaan kaupunki</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>235</ts1:id>
                    <ts1:name_en>City of Kauniainen</ts1:name_en>
                    <ts1:name_sv>Grankulla stad</ts1:name_sv>
                    <ts1:data_source_url>www.kauniainen.fi</ts1:data_source_url>
                    <ts1:name_fi>Kauniaisten kaupunki</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1000</ts1:id>
                    <ts1:name_en>State IT Service Centre, Suomi.fi editorial team</ts1:name_en>
                    <ts1:name_sv>Statens IT-servicecentral, Suomi.fi-redaktionen</ts1:name_sv>
                    <ts1:data_source_url>www.suomi.fi</ts1:data_source_url>
                    <ts1:name_fi>Valtion IT-palvelukeskus, Suomi.fi-toimitus</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1001</ts1:id>
                    <ts1:name_en>HUS Hospital District</ts1:name_en>
                    <ts1:name_sv>Samkommunen HNS</ts1:name_sv>
                    <ts1:data_source_url>www.hus.fi</ts1:data_source_url>
                    <ts1:name_fi>HUS-kuntayhtymä</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1002</ts1:id>
                    <ts1:name_en>Helsinki Marketing Ltd</ts1:name_en>
                    <ts1:name_sv>Helsinki Marketing Ltd</ts1:name_sv>
                    <ts1:data_source_url>www.visithelsinki.fi</ts1:data_source_url>
                    <ts1:name_fi>Helsingin markkinointi Oy</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1003</ts1:id>
                    <ts1:name_en>Ministry of Justice, Election Unit</ts1:name_en>
                    <ts1:name_sv>Justitieministeriet, den högsta valmyndigheten</ts1:name_sv>
                    <ts1:data_source_url>www.vaalit.fi</ts1:data_source_url>
                    <ts1:name_fi>Oikeusministeriö, ylin vaaliviranomainen</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1004</ts1:id>
                    <ts1:name_en>Helsinki Region Environmental Services Authority HSY</ts1:name_en>
                    <ts1:name_sv>Helsingforsregionens miljötjänster HRM</ts1:name_sv>
                    <ts1:data_source_url>www.hsy.fi</ts1:data_source_url>
                    <ts1:name_fi>Helsingin seudun ympäristöpalvelut HSY</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1005</ts1:id>
                    <ts1:name_en>Service Map editorial team</ts1:name_en>
                    <ts1:name_sv>Servicekartans redaktion</ts1:name_sv>
                    <ts1:data_source_url>www.hel.fi/palvelukartta</ts1:data_source_url>
                    <ts1:name_fi>Palvelukartan toimitus</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1007</ts1:id>
                    <ts1:name_en>JLY - Finnish Solid Waste Association</ts1:name_en>
                    <ts1:name_sv>JLY - Avfallsverksföreningen rf</ts1:name_sv>
                    <ts1:data_source_url>www.jly.fi</ts1:data_source_url>
                    <ts1:name_fi>JLY Jätelaitosyhdistys ry</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1008</ts1:id>
                    <ts1:name_en>The Norwegian Electric Vehicle Association</ts1:name_en>
                    <ts1:name_sv>Norsk Elbilforening</ts1:name_sv>
                    <ts1:data_source_url>NOBIL.no</ts1:data_source_url>
                    <ts1:name_fi>Norsk Elbilforening, sähköautojen latauspisteet</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1009</ts1:id>
                    <ts1:name_en>External service point register user society</ts1:name_en>
                    <ts1:name_sv>Gemenskapen bakom externa serviceregister</ts1:name_sv>
                    <ts1:data_source_url>asiointi.hel.fi/tprulkoinen</ts1:data_source_url>
                    <ts1:name_fi>Ulkoisen toimipisterekisterin käyttäjäyhteisö</ts1:name_fi>
                </ts1:array>
                <ts1:array>
                    <ts1:id>1010</ts1:id>
                    <ts1:name_en>University of Jyväskylä, LIPAS Liikuntapaikat.fi</ts1:name_en>
                    <ts1:name_sv>Jyväskylä universitet, LIPAS Liikuntapaikat.fi</ts1:name_sv>
                    <ts1:data_source_url>www.liikuntapaikat.fi</ts1:data_source_url>
                    <ts1:name_fi>Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi</ts1:name_fi>
                </ts1:array>
            </ts1:response>
        </ts1:getOrganizationListResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

#### REST API for City of Helsinki Service Map - Single Organization

API documentation: http://www.hel.fi/palvelukarttaws/rest/ver2_en.html

#### Consumer Gateway

Configuration:
```
1.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganization.v1
1.path=/www.hel.fi/palvelukarttaws/rest/v2/organization/{resourceId}
1.verb=get
```

Service request:

* Method: ```GET```
* Accept: ```text/xml``` or ```application/json```
* X-XRd-MessageId: ```1```
* X-XRd-UserId: ```test```

URL:

```
http://localhost:8080/rest-adapter-service/Consumer/www.hel.fi/palvelukarttaws/rest/v2/organization/49
```

Browser-based access:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1/49?X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

Consumer Gateway response:

```
{
    "id": 49,
    "name_en": "City of Espoo",
    "name_sv": "Esbo stad",
    "data_source_url": "www.espoo.fi",
    "name_fi": "Espoon kaupunki"
}
```

#### Provider Gateway
Configuration:
```
1.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganization.v1
1.url=http://www.hel.fi/palvelukarttaws/rest/v2/organization/
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
            <id:serviceCode>getOrganization</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
		<xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <test:getOrganization xmlns:test="http://x-road.global/producer">
            <test:request>
                <resourceId>49</resourceId>
            </test:request>
        </test:getOrganization>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

API response URL: http://www.hel.fi/palvelukarttaws/rest/v2/organization/49

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
            <id:serviceCode>getOrganization</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
		<xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <ts1:getOrganizationResponse xmlns:ts1="http://x-road.global/producer">
            <ts1:request>
                <ts1:resourceId>49</ts1:resourceId>
            </ts1:request>
            <ts1:response>
                <ts1:id>49</ts1:id>
                <ts1:name_en>City of Espoo</ts1:name_en>
                <ts1:name_sv>Esbo stad</ts1:name_sv>
                <ts1:data_source_url>www.espoo.fi</ts1:data_source_url>
                <ts1:name_fi>Espoon kaupunki</ts1:name_fi>
            </ts1:response>
        </ts1:getOrganizationResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

#### Library Directory

API documentation (in Finnish): https://api.kirjastot.fi/

#### Consumer Gateway

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
   "result":{
      "total":1,
      "count":1,
      "items":{
         "organisation":{
            "parent":84846,
            "web_library":"",
            "address":{
               "area":{
                  "multilang":true,
                  "value":[
                     {
                        "lang":"fi",
                        "content":"Kallio"
                     },
                     {
                        "lang":"sv"
                     },
                     {
                        "lang":"en"
                     },
                     {
                        "lang":"se"
                     },
                     {
                        "lang":"ru",
                        "content":"Kallio"
                     }
                  ]
               },
               "zipcode":"00530",
               "box_number":"",
               "city":{
                  "multilang":true,
                  "value":[
                     {
                        "lang":"fi",
                        "content":"Helsinki"
                     },
                     {
                        "lang":"sv",
                        "content":"Helsingfors"
                     },
                     {
                        "lang":"en",
                        "content":"Helsinki"
                     },
                     {
                        "lang":"se",
                        "content":"Helsinki"
                     },
                     {
                        "lang":"ru",
                        "content":"Helsinki"
                     }
                  ]
               },
               "street":{
                  "multilang":true,
                  "value":[
                     {
                        "lang":"fi",
                        "content":"Viides linja 11"
                     },
                     {
                        "lang":"sv",
                        "content":"Femte linjen 11"
                     },
                     {
                        "lang":"en",
                        "content":"Viides linja 11"
                     },
                     {
                        "lang":"se"
                     },
                     {
                        "lang":"ru",
                        "content":"Viides linja 11"
                     }
                  ]
               },
               "coordinates":{
                  "lon":24.95355311,
                  "lat":60.18372258
               }
            },
            "city":15885,
            "consortium":2093,
            "type":"library",
            "provincial_library":396,
            "branch_type":"library",
            "name":{
               "multilang":true,
               "value":[
                  {
                     "lang":"fi",
                     "content":"Kallion kirjasto"
                  },
                  {
                     "lang":"sv",
                     "content":"Berghälls bibliotek"
                  },
                  {
                     "lang":"en",
                     "content":"Kallio Library"
                  },
                  {
                     "lang":"se"
                  },
                  {
                     "lang":"ru",
                     "content":"Библиотека Каллио"
                  }
               ]
            },
            "short_name":{
               "multilang":true,
               "value":[
                  {
                     "lang":"fi",
                     "content":"Kallio"
                  },
                  {
                     "lang":"sv",
                     "content":"Berghäll"
                  },
                  {
                     "lang":"en",
                     "content":"Kallio"
                  },
                  {
                     "lang":"se"
                  },
                  {
                     "lang":"ru",
                     "content":"Каллио"
                  }
               ]
            },
            "id":84860,
            "region":1001,
            "email":{
               "multilang":true,
               "value":[
                  {
                     "lang":"fi",
                     "content":"kallion_kirjasto@hel.fi"
                  },
                  {
                     "lang":"sv",
                     "content":"kallion_kirjasto@hel.fi"
                  },
                  {
                     "lang":"en",
                     "content":"kallion_kirjasto@hel.fi"
                  },
                  {
                     "lang":"se"
                  },
                  {
                     "lang":"ru"
                  }
               ]
            },
            "slug":{
               "multilang":true,
               "value":[
                  {
                     "lang":"fi",
                     "content":"kallio"
                  },
                  {
                     "lang":"sv",
                     "content":"kallio"
                  },
                  {
                     "lang":"en",
                     "content":"kallio"
                  },
                  {
                     "lang":"se",
                     "content":"kallio"
                  },
                  {
                     "lang":"ru",
                     "content":"kallio"
                  }
               ]
            },
            "homepage":{
               "multilang":true,
               "value":[
                  {
                     "lang":"fi",
                     "content":"http://www.helmet.fi/kallionkirjasto"
                  },
                  {
                     "lang":"sv",
                     "content":"http://www.helmet.fi/berghallsbibliotek"
                  },
                  {
                     "lang":"en",
                     "content":"http://www.helmet.fi/kalliolibrary"
                  },
                  {
                     "lang":"se"
                  },
                  {
                     "lang":"ru",
                     "content":"http://www.helmet.fi/kallionkirjasto"
                  }
               ]
            }
         }
      }
   }
}
```

#### Provider Gateway

Configuration:
```
2.id=FI_PILOT.GOV.1019125-0.Demo2Service.getLibrary.v1
2.url=https://api.kirjastot.fi/v3/organisation?format=xml
```
Service request:

Method: ```POST```
Content-Type: ```text/xml```

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
<SOAP-ENV:Envelope>
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
        <ts1:getLibraryResponse>
            <ts1:request>
                <ts1:name>Kallio</ts1:name>
                <ts1:city.name>Helsinki</ts1:city.name>
            </ts1:request>
            <ts1:response>
                <result count="1" total="1">
                    <items>
                        <organisation>
                            <web_library/>
                            <parent>84846</parent>
                            <branch_type>library</branch_type>
                            <short_name multilang="true">
                                <value lang="fi">Kallio</value>
                                <value lang="sv">BerghÃ¤ll</value>
                                <value lang="en">Kallio</value>
                                <value lang="se"/>
                                <value lang="ru">ÐšÐ°Ð»Ð»Ð¸Ð¾</value>
                            </short_name>
                            <type>library</type>
                            <homepage multilang="true">
                                <value lang="fi">http://www.helmet.fi/kallionkirjasto</value>
                                <value lang="sv">http://www.helmet.fi/berghallsbibliotek</value>
                                <value lang="en">http://www.helmet.fi/kalliolibrary</value>
                                <value lang="se"/>
                                <value lang="ru">http://www.helmet.fi/kallionkirjasto</value>
                            </homepage>
                            <city>15885</city>
                            <id>84860</id>
                            <consortium>2093</consortium>
                            <address>
                                <area multilang="true">
                                    <value lang="fi">Kallio</value>
                                    <value lang="sv"/>
                                    <value lang="en"/>
                                    <value lang="se"/>
                                    <value lang="ru">Kallio</value>
                                </area>
                                <street multilang="true">
                                    <value lang="fi">Viides linja 11</value>
                                    <value lang="sv">Femte linjen 11</value>
                                    <value lang="en">Viides linja 11</value>
                                    <value lang="se"/>
                                    <value lang="ru">Viides linja 11</value>
                                </street>
                                <zipcode>00530</zipcode>
                                <box_number/>
                                <coordinates>
                                    <lon>24.95355311</lon>
                                    <lat>60.18372258</lat>
                                </coordinates>
                                <city multilang="true">
                                    <value lang="fi">Helsinki</value>
                                    <value lang="sv">Helsingfors</value>
                                    <value lang="en">Helsinki</value>
                                    <value lang="se">Helsinki</value>
                                    <value lang="ru">Helsinki</value>
                                </city>
                            </address>
                            <email multilang="true">
                                <value lang="fi">kallion_kirjasto@hel.fi</value>
                                <value lang="sv">kallion_kirjasto@hel.fi</value>
                                <value lang="en">kallion_kirjasto@hel.fi</value>
                                <value lang="se"/>
                                <value lang="ru"/>
                            </email>
                            <name multilang="true">
                                <value lang="fi">Kallion kirjasto</value>
                                <value lang="sv">BerghÃ¤lls bibliotek</value>
                                <value lang="en">Kallio Library</value>
                                <value lang="se"/>
                                <value lang="ru">Ð‘Ð¸Ð±Ð»Ð¸Ð¾Ñ‚ÐµÐºÐ° ÐšÐ°Ð»Ð»Ð¸Ð¾</value>
                            </name>
                            <slug multilang="true">
                                <value lang="fi">kallio</value>
                                <value lang="sv">kallio</value>
                                <value lang="en">kallio</value>
                                <value lang="se">kallio</value>
                                <value lang="ru">kallio</value>
                            </slug>
                            <region>1001</region>
                            <provincial_library>396</provincial_library>
                        </organisation>
                    </items>
                </result>
            </ts1:response>
        </ts1:getLibraryResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
#### Finnish Patent and Registration Office - Business Information Search

API documentation: http://avoindata.prh.fi/ytj_en.html

#### Consumer Gateway

Configuration:
```
3.id=FI_PILOT.GOV.1019125-0.Demo2Service.searchCompany.v1
3.path=/avoindata.prh.fi/opendata/bis/v1/
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
http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/?totalResults=false&resultsFrom=0&name=asunto&companyRegistrationFrom=2016-02-28
```

Browser-based access:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.searchCompany.v1/?totalResults=false&resultsFrom=0&name=asunto&companyRegistrationFrom=2015-02-28&X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

Consumer Gateway response:

```
{
   "resultsFrom":0,
   "totalResults":-1,
   "nextResultsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1?resultsFrom=10&totalResults=false&companyRegistrationFrom=2016-02-28&name=asunto",
   "exceptionNoticeUri":null,
   "previousResultsUri":null,
   "type":"fi.prh.opendata.bis",
   "version":1,
   "results":[
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786724-5",
         "businessId":"2786724-5",
         "name":"Asunto Oy Kuusamon Tetra 2",
         "registrationDate":"2016-10-06",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786552-2",
         "businessId":"2786552-2",
         "name":"Asunto Oy Auran Jokihovi",
         "registrationDate":"2016-10-06",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786587-2",
         "businessId":"2786587-2",
         "name":"Asunto Oy Espoon Lintukartano",
         "registrationDate":"2016-10-06",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786499-2",
         "businessId":"2786499-2",
         "name":"Asunto Oy Casa Ora, Espoo",
         "registrationDate":"2016-10-06",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786082-8",
         "businessId":"2786082-8",
         "name":"Asunto Oy Vantaan Neidonkenkä",
         "registrationDate":"2016-10-05",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786320-1",
         "businessId":"2786320-1",
         "name":"Asunto Oy Leppäveden Kytölä",
         "registrationDate":"2016-10-05",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786421-2",
         "businessId":"2786421-2",
         "name":"Asunto Oy Espoon Jaakobinsauva",
         "registrationDate":"2016-10-05",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786191-9",
         "businessId":"2786191-9",
         "name":"Asunto Oy Harjavallan Pistokuja",
         "registrationDate":"2016-10-05",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2786087-9",
         "businessId":"2786087-9",
         "name":"Asunto Oy Vantaan Pikkulehdokki",
         "registrationDate":"2016-10-05",
         "companyForm":"AOY"
      },
      {
         "detailsUri":"http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2785923-7",
         "businessId":"2785923-7",
         "name":"Asunto Oy Espoon Tapiolan Taika",
         "registrationDate":"2016-10-04",
         "companyForm":"AOY"
      }
   ]
}
```

#### Provider Gateway

Configuration:
```
3.id=FI_PILOT.GOV.1019125-0.Demo2Service.searchCompany.v1
3.url=http://avoindata.prh.fi/bis/v1
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
                <companyRegistrationFrom>2016-02-28</companyRegistrationFrom>
            </test:request>
        </test:searchCompany>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

API results URL: http://avoindata.prh.fi/bis/v1?totalResults=false&maxResults=10&resultsFrom=0&name=asunto&companyRegistrationFrom=2015-02-28

Rest Adapter Service response:
```
<SOAP-ENV:Envelope>
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
        <ts1:searchCompanyResponse>
            <ts1:request>
                <ts1:totalResults>false</ts1:totalResults>
                <ts1:maxResults>10</ts1:maxResults>
                <ts1:resultsFrom>0</ts1:resultsFrom>
                <ts1:name>asunto</ts1:name>
                <ts1:companyRegistrationFrom>2016-02-28</ts1:companyRegistrationFrom>
            </ts1:request>
            <ts1:response>
                <ts1:resultsFrom>0</ts1:resultsFrom>
                <ts1:totalResults>-1</ts1:totalResults>
                <ts1:nextResultsUri>
                    http://avoindata.prh.fi/opendata/bis/v1?resultsFrom=10&totalResults=false&companyRegistrationFrom=2016-02-28&maxResults=10&name=asunto
                </ts1:nextResultsUri>
                <ts1:exceptionNoticeUri>null</ts1:exceptionNoticeUri>
                <ts1:previousResultsUri>null</ts1:previousResultsUri>
                <ts1:type>fi.prh.opendata.bis</ts1:type>
                <ts1:version>1</ts1:version>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786724-5</ts1:detailsUri>
                    <ts1:businessId>2786724-5</ts1:businessId>
                    <ts1:name>Asunto Oy Kuusamon Tetra 2</ts1:name>
                    <ts1:registrationDate>2016-10-06</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786552-2</ts1:detailsUri>
                    <ts1:businessId>2786552-2</ts1:businessId>
                    <ts1:name>Asunto Oy Auran Jokihovi</ts1:name>
                    <ts1:registrationDate>2016-10-06</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786587-2</ts1:detailsUri>
                    <ts1:businessId>2786587-2</ts1:businessId>
                    <ts1:name>Asunto Oy Espoon Lintukartano</ts1:name>
                    <ts1:registrationDate>2016-10-06</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786499-2</ts1:detailsUri>
                    <ts1:businessId>2786499-2</ts1:businessId>
                    <ts1:name>Asunto Oy Casa Ora, Espoo</ts1:name>
                    <ts1:registrationDate>2016-10-06</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786082-8</ts1:detailsUri>
                    <ts1:businessId>2786082-8</ts1:businessId>
                    <ts1:name>Asunto Oy Vantaan Neidonkenkä</ts1:name>
                    <ts1:registrationDate>2016-10-05</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786320-1</ts1:detailsUri>
                    <ts1:businessId>2786320-1</ts1:businessId>
                    <ts1:name>Asunto Oy Leppäveden Kytölä</ts1:name>
                    <ts1:registrationDate>2016-10-05</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786421-2</ts1:detailsUri>
                    <ts1:businessId>2786421-2</ts1:businessId>
                    <ts1:name>Asunto Oy Espoon Jaakobinsauva</ts1:name>
                    <ts1:registrationDate>2016-10-05</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786191-9</ts1:detailsUri>
                    <ts1:businessId>2786191-9</ts1:businessId>
                    <ts1:name>Asunto Oy Harjavallan Pistokuja</ts1:name>
                    <ts1:registrationDate>2016-10-05</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2786087-9</ts1:detailsUri>
                    <ts1:businessId>2786087-9</ts1:businessId>
                    <ts1:name>Asunto Oy Vantaan Pikkulehdokki</ts1:name>
                    <ts1:registrationDate>2016-10-05</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
                <ts1:results>
                    <ts1:detailsUri>http://avoindata.prh.fi/opendata/bis/v1/2785923-7</ts1:detailsUri>
                    <ts1:businessId>2785923-7</ts1:businessId>
                    <ts1:name>Asunto Oy Espoon Tapiolan Taika</ts1:name>
                    <ts1:registrationDate>2016-10-04</ts1:registrationDate>
                    <ts1:companyForm>AOY</ts1:companyForm>
                </ts1:results>
            </ts1:response>
        </ts1:searchCompanyResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

#### Finnish Patent and Registration Office - Get Company

API documentation: http://avoindata.prh.fi/ytj_en.html

#### Consumer Gateway

Configuration:
```
4.id=FI_PILOT.GOV.1019125-0.Demo2Service.getCompany.v1
4.path=/avoindata.prh.fi/opendata/bis/v1/{resourceId}
4.verb=get
4.response.modurl=true
```

Service request:

* Method: ```GET```
* Accept: ```text/xml``` or ```application/json```
* X-XRd-MessageId: ```1```
* X-XRd-UserId: ```test```

URL:
```
http://localhost:8080/rest-adapter-service/Consumer/avoindata.prh.fi/opendata/bis/v1/2663307-6
```

Browser-based access:

```
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.getCompany.v1/2663307-6/?X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks&Accept=application/json
```

Consumer Gateway response:

```
{
   "resultsFrom":0,
   "totalResults":-1,
   "nextResultsUri":null,
   "exceptionNoticeUri":null,
   "previousResultsUri":null,
   "type":"fi.prh.opendata.bis",
   "version":1,
   "results":{
      "detailsUri":null,
      "registeredEntries":[
         {
            "statusDate":"2015-04-09",
            "endDate":null,
            "authority":2,
            "registrationDate":"2015-04-09",
            "description":"Ei rekisteröity perustaminen",
            "language":"FI",
            "register":1,
            "status":1
         },
         {
            "statusDate":"2015-04-09",
            "endDate":null,
            "authority":2,
            "registrationDate":"2015-04-09",
            "description":"Oregistrerat grundande",
            "language":"SE",
            "register":1,
            "status":1
         },
         {
            "statusDate":"2015-04-09",
            "endDate":null,
            "authority":2,
            "registrationDate":"2015-04-09",
            "description":"Start-up not registered",
            "language":"EN",
            "register":1,
            "status":1
         },
         {
            "statusDate":"2014-12-30",
            "endDate":"2015-04-08",
            "authority":2,
            "registrationDate":"2014-12-30",
            "description":"Rekisteröimätön",
            "language":"FI",
            "register":1,
            "status":2
         },
         {
            "statusDate":"2014-12-30",
            "endDate":"2015-04-08",
            "authority":2,
            "registrationDate":"2014-12-30",
            "description":"Oregistrerad",
            "language":"SE",
            "register":1,
            "status":2
         },
         {
            "statusDate":"2014-12-30",
            "endDate":"2015-04-08",
            "authority":2,
            "registrationDate":"2014-12-30",
            "description":"Unregistered",
            "language":"EN",
            "register":1,
            "status":2
         }
      ],
      "businessId":"2663307-6",
      "companyForms":[
         {
            "endDate":null,
            "name":"Osakeyhtiö",
            "registrationDate":"2014-12-30",
            "language":"FI",
            "source":3,
            "type":"OY",
            "version":1
         },
         {
            "endDate":null,
            "name":"Aktiebolag",
            "registrationDate":"2014-12-30",
            "language":"SE",
            "source":3,
            "type":"AB",
            "version":1
         },
         {
            "endDate":null,
            "name":"Limited company",
            "registrationDate":"2014-12-30",
            "language":"EN",
            "source":3,
            "type":null,
            "version":1
         }
      ],
      "name":null,
      "registrationDate":"2014-12-30",
      "businessIdChanges":{
         "reason":0,
         "change":0,
         "changeDate":"2015-05-07",
         "description":null,
         "language":null,
         "source":3,
         "newBusinessId":null,
         "oldBusinessId":"2663307-6"
      },
      "companyForm":"OY"
   }
}
```

#### Provider Gateway

Configuration:
```
4.id=FI_PILOT.GOV.1019125-0.Demo2Service.getCompany.v1
4.url=http://avoindata.prh.fi/bis/v1
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
            <id:serviceCode>getCompany</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
		<xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <test:getCompany xmlns:test="http://x-road.global/producer">
            <test:request>
                <resourceId>2663307-6</resourceId>
            </test:request>
        </test:getCompany>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

API response URL: http://avoindata.prh.fi/bis/v1/2663307-6

Rest Adapter Service response:
```
<SOAP-ENV:Envelope>
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
            <id:serviceCode>getCompany</id:serviceCode>
            <id:serviceVersion>v1</id:serviceVersion>
        </xrd:service>
        <xrd:userId>test</xrd:userId>
        <xrd:id>0ba036ea-d612-4e74-bf73-59a6f15627c8</xrd:id>
        <xrd:protocolVersion>4.0</xrd:protocolVersion>
    </SOAP-ENV:Header>
    <SOAP-ENV:Body>
        <ts1:getCompanyResponse>
            <ts1:request>
                <ts1:resourceId>2663307-6</ts1:resourceId>
            </ts1:request>
            <ts1:response>
                <ts1:resultsFrom>0</ts1:resultsFrom>
                <ts1:totalResults>-1</ts1:totalResults>
                <ts1:nextResultsUri>null</ts1:nextResultsUri>
                <ts1:exceptionNoticeUri>null</ts1:exceptionNoticeUri>
                <ts1:previousResultsUri>null</ts1:previousResultsUri>
                <ts1:type>fi.prh.opendata.bis</ts1:type>
                <ts1:version>1</ts1:version>
                <ts1:results>
                    <ts1:detailsUri>null</ts1:detailsUri>
                    <ts1:registeredEntries>
                        <ts1:statusDate>2015-04-09</ts1:statusDate>
                        <ts1:endDate>null</ts1:endDate>
                        <ts1:authority>2</ts1:authority>
                        <ts1:registrationDate>2015-04-09</ts1:registrationDate>
                        <ts1:description>Ei rekisteröity perustaminen</ts1:description>
                        <ts1:language>FI</ts1:language>
                        <ts1:register>1</ts1:register>
                        <ts1:status>1</ts1:status>
                    </ts1:registeredEntries>
                    <ts1:registeredEntries>
                        <ts1:statusDate>2015-04-09</ts1:statusDate>
                        <ts1:endDate>null</ts1:endDate>
                        <ts1:authority>2</ts1:authority>
                        <ts1:registrationDate>2015-04-09</ts1:registrationDate>
                        <ts1:description>Oregistrerat grundande</ts1:description>
                        <ts1:language>SE</ts1:language>
                        <ts1:register>1</ts1:register>
                        <ts1:status>1</ts1:status>
                    </ts1:registeredEntries>
                    <ts1:registeredEntries>
                        <ts1:statusDate>2015-04-09</ts1:statusDate>
                        <ts1:endDate>null</ts1:endDate>
                        <ts1:authority>2</ts1:authority>
                        <ts1:registrationDate>2015-04-09</ts1:registrationDate>
                        <ts1:description>Start-up not registered</ts1:description>
                        <ts1:language>EN</ts1:language>
                        <ts1:register>1</ts1:register>
                        <ts1:status>1</ts1:status>
                    </ts1:registeredEntries>
                    <ts1:registeredEntries>
                        <ts1:statusDate>2014-12-30</ts1:statusDate>
                        <ts1:endDate>2015-04-08</ts1:endDate>
                        <ts1:authority>2</ts1:authority>
                        <ts1:registrationDate>2014-12-30</ts1:registrationDate>
                        <ts1:description>Rekisteröimätön</ts1:description>
                        <ts1:language>FI</ts1:language>
                        <ts1:register>1</ts1:register>
                        <ts1:status>2</ts1:status>
                    </ts1:registeredEntries>
                    <ts1:registeredEntries>
                        <ts1:statusDate>2014-12-30</ts1:statusDate>
                        <ts1:endDate>2015-04-08</ts1:endDate>
                        <ts1:authority>2</ts1:authority>
                        <ts1:registrationDate>2014-12-30</ts1:registrationDate>
                        <ts1:description>Oregistrerad</ts1:description>
                        <ts1:language>SE</ts1:language>
                        <ts1:register>1</ts1:register>
                        <ts1:status>2</ts1:status>
                    </ts1:registeredEntries>
                    <ts1:registeredEntries>
                        <ts1:statusDate>2014-12-30</ts1:statusDate>
                        <ts1:endDate>2015-04-08</ts1:endDate>
                        <ts1:authority>2</ts1:authority>
                        <ts1:registrationDate>2014-12-30</ts1:registrationDate>
                        <ts1:description>Unregistered</ts1:description>
                        <ts1:language>EN</ts1:language>
                        <ts1:register>1</ts1:register>
                        <ts1:status>2</ts1:status>
                    </ts1:registeredEntries>
                    <ts1:businessId>2663307-6</ts1:businessId>
                    <ts1:companyForms>
                        <ts1:endDate>null</ts1:endDate>
                        <ts1:name>Osakeyhtiö</ts1:name>
                        <ts1:registrationDate>2014-12-30</ts1:registrationDate>
                        <ts1:language>FI</ts1:language>
                        <ts1:source>3</ts1:source>
                        <ts1:type>OY</ts1:type>
                        <ts1:version>1</ts1:version>
                    </ts1:companyForms>
                    <ts1:companyForms>
                        <ts1:endDate>null</ts1:endDate>
                        <ts1:name>Aktiebolag</ts1:name>
                        <ts1:registrationDate>2014-12-30</ts1:registrationDate>
                        <ts1:language>SE</ts1:language>
                        <ts1:source>3</ts1:source>
                        <ts1:type>AB</ts1:type>
                        <ts1:version>1</ts1:version>
                    </ts1:companyForms>
                    <ts1:companyForms>
                        <ts1:endDate>null</ts1:endDate>
                        <ts1:name>Limited company</ts1:name>
                        <ts1:registrationDate>2014-12-30</ts1:registrationDate>
                        <ts1:language>EN</ts1:language>
                        <ts1:source>3</ts1:source>
                        <ts1:type>null</ts1:type>
                        <ts1:version>1</ts1:version>
                    </ts1:companyForms>
                    <ts1:companyForm>OY</ts1:companyForm>
                    <ts1:name>null</ts1:name>
                    <ts1:registrationDate>2014-12-30</ts1:registrationDate>
                    <ts1:businessIdChanges>
                        <ts1:reason>0</ts1:reason>
                        <ts1:change>0</ts1:change>
                        <ts1:changeDate>2015-05-07</ts1:changeDate>
                        <ts1:description>null</ts1:description>
                        <ts1:language>null</ts1:language>
                        <ts1:source>3</ts1:source>
                        <ts1:newBusinessId>null</ts1:newBusinessId>
                        <ts1:oldBusinessId>2663307-6</ts1:oldBusinessId>
                    </ts1:businessIdChanges>
                </ts1:results>
            </ts1:response>
        </ts1:getCompanyResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

#### Finto : Finnish Thesaurus and Ontology Service - Search

API documentation: http://api.finto.fi/
#### Consumer Gateway

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
http://localhost:8080/rest-adapter-service/Consumer/FI_PILOT.GOV.1019125-0.Demo2Service.fintoService.v1/?query=cat&lang=en&X-XRd-UserId=test&X-XRd-MessageId=1&X-XRd-NamespaceSerialize=http://x-road.global/producer&X-XRd-NamespacePrefixSerialize=ks
```

Consumer  Gateway response:

```
{
   "@context":{
      "hiddenLabel":"skos:hiddenLabel",
      "prefLabel":"skos:prefLabel",
      "skos":"http://www.w3.org/2004/02/skos/core#",
      "isothes":"http://purl.org/iso25964/skos-thes#",
      "onki":"http://schema.onki.fi/onki#",
      "altLabel":"skos:altLabel",
      "type":"@type",
      "@language":"en",
      "uri":"@id",
      "results":{
         "@container":"@list",
         "@id":"onki:results"
      }
   },
   "uri":"",
   "results":[
      {
         "notation":"cat",
         "prefLabel":"Catalan language",
         "vocab":"lexvo",
         "type":[
            "skos:Concept",
            "http://lexvo.org/ontology#Language"
         ],
         "lang":"en",
         "uri":"http://lexvo.org/id/iso639-3/cat"
      },
      {
         "notation":"cat",
         "prefLabel":"???????",
         "vocab":"lexvo",
         "type":[
            "skos:Concept",
            "http://lexvo.org/ontology#Language"
         ],
         "lang":"en-Dsrt",
         "uri":"http://lexvo.org/id/iso639-3/cat"
      },
      {
         "notation":"34B12",
         "prefLabel":"cat",
         "vocab":"ic",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://iconclass.org/34B12"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"afo",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "prefLabel":"cat",
         "vocab":"afo",
         "type":[
            "skos:Concept",
            "http://www.yso.fi/onto/afo-meta/Concept"
         ],
         "lang":"en",
         "uri":"http://www.yso.fi/onto/afo/p1287"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"juho",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"jupo",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"kauno",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"keko",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"kito",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "prefLabel":"cat",
         "vocab":"koko",
         "type":[
            "skos:Concept",
            "http://www.yso.fi/onto/afo-meta/Concept",
            "http://www.yso.fi/onto/kauno-meta/Concept",
            "http://www.yso.fi/onto/yso-meta/Concept"
         ],
         "lang":"en",
         "uri":"http://www.yso.fi/onto/koko/p37252"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"kto",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"kulo",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"liito",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"mero",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "hiddenLabel":"Cat",
         "prefLabel":"Cats",
         "vocab":"mesh",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/mesh/D002415"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"muso",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"pto",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"puho",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"maotao",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "prefLabel":"cat",
         "vocab":"tero",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/tero/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"tsr",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "exvocab":"yso",
         "prefLabel":"cat",
         "vocab":"valo",
         "type":"skos:Concept",
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      },
      {
         "prefLabel":"cat",
         "vocab":"yso",
         "type":[
            "skos:Concept",
            "http://www.yso.fi/onto/yso-meta/Concept"
         ],
         "lang":"en",
         "uri":"http://www.yso.fi/onto/yso/p19378"
      }
   ]
}
```

#### Provider Gateway

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
<SOAP-ENV:Envelope>
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
        <ts1:fintoServiceResponse>
            <ts1:request>
                <ts1:query>cat</ts1:query>
                <ts1:lang>en</ts1:lang>
            </ts1:request>
            <ts1:response>
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
                    <ts1:notation>cat</ts1:notation>
                    <ts1:prefLabel>Catalan language</ts1:prefLabel>
                    <ts1:vocab>lexvo</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:type>http://lexvo.org/ontology#Language</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://lexvo.org/id/iso639-3/cat</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:notation>cat</ts1:notation>
                    <ts1:prefLabel>???????</ts1:prefLabel>
                    <ts1:vocab>lexvo</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:type>http://lexvo.org/ontology#Language</ts1:type>
                    <ts1:lang>en-Dsrt</ts1:lang>
                    <ts1:uri>http://lexvo.org/id/iso639-3/cat</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:notation>34B12</ts1:notation>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>ic</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://iconclass.org/34B12</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>afo</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
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
                    <ts1:vocab>juho</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>jupo</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>kauno</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
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
                    <ts1:type>http://www.yso.fi/onto/afo-meta/Concept</ts1:type>
                    <ts1:type>http://www.yso.fi/onto/kauno-meta/Concept</ts1:type>
                    <ts1:type>http://www.yso.fi/onto/yso-meta/Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/koko/p37252</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>kto</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>kulo</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>liito</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>mero</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:hiddenLabel>Cat</ts1:hiddenLabel>
                    <ts1:prefLabel>Cats</ts1:prefLabel>
                    <ts1:vocab>mesh</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/mesh/D002415</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>muso</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>pto</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>puho</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>maotao</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>tero</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/tero/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>tsr</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
                    <ts1:lang>en</ts1:lang>
                    <ts1:uri>http://www.yso.fi/onto/yso/p19378</ts1:uri>
                </ts1:results>
                <ts1:results>
                    <ts1:exvocab>yso</ts1:exvocab>
                    <ts1:prefLabel>cat</ts1:prefLabel>
                    <ts1:vocab>valo</ts1:vocab>
                    <ts1:type>skos:Concept</ts1:type>
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
            </ts1:response>
        </ts1:fintoServiceResponse>
    </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
