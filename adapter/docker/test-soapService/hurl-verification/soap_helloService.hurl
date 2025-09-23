POST http://example-adapter:8080/example-adapter/Endpoint
Content-Type: text/xml;charset=utf-8
Accept: text/xml

[Options]
retry: 20
retry-interval: 5000
compressed: true

file,./examples/helloServiceRequest.xml;

HTTP 200

[Asserts]
status == 200

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:iden="http://x-road.eu/xsd/identifiers" xmlns:xro="http://x-road.eu/xsd/xroad.xsd">
    <soapenv:Header>
        <xro:client iden:objectType="SUBSYSTEM">
            <iden:xRoadInstance>FI_TEST</iden:xRoadInstance>
            <iden:memberClass>GOV</iden:memberClass>
            <iden:memberCode>1234567-8</iden:memberCode>
            <iden:subsystemCode>TestClient</iden:subsystemCode>
        </xro:client>
        <xro:service iden:objectType="SERVICE">
            <iden:xRoadInstance>FI_TEST</iden:xRoadInstance>
            <iden:memberClass>GOV</iden:memberClass>
            <iden:memberCode>9876543-1</iden:memberCode>
            <iden:subsystemCode>DemoService</iden:subsystemCode>
            <iden:serviceCode>helloService</iden:serviceCode>
            <iden:serviceVersion>v1</iden:serviceVersion>
        </xro:service>
        <xro:id>ID11234</xro:id>
        <xro:userId>EE1234567890</xro:userId>
        <xro:protocolVersion>4.0</xro:protocolVersion>
    </soapenv:Header>
    <soapenv:Body><ts1:helloServiceResponse xmlns:ts1="http://test.x-road.global/producer"><ts1:message>Hello Test! Greetings from adapter server!</ts1:message></ts1:helloServiceResponse></soapenv:Body>
</soapenv:Envelope>
```
