POST http://adapter:8080/rest-adapter-service/Provider
Content-Type: text/xml;charset=utf-8
Accept: text/xml

[Options]
retry: 20
retry-interval: 5000

file,./soapRequests/format1.xml;

HTTP 200

[Asserts]
status == 200

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:iden="http://x-road.eu/xsd/identifiers" xmlns:ts1="http://test.x-road.global/producer" xmlns:xro="http://x-road.eu/xsd/xroad.xsd">
    <soapenv:Header>
        <xro:client iden:objectType="SUBSYSTEM">
            <iden:xRoadInstance>DEV</iden:xRoadInstance>
            <iden:memberClass>GOV</iden:memberClass>
            <iden:memberCode>Test</iden:memberCode>
            <iden:subsystemCode>Test</iden:subsystemCode>
        </xro:client>
        <xro:service iden:objectType="SERVICE">
            <iden:xRoadInstance>FI_TEST</iden:xRoadInstance>
            <iden:memberClass>GOV</iden:memberClass>
            <iden:memberCode>9876543-1</iden:memberCode>
            <iden:subsystemCode>Wiremock</iden:subsystemCode>
            <iden:serviceCode>format1_plain</iden:serviceCode>
            <iden:serviceVersion>v1</iden:serviceVersion>
        </xro:service>
        <xro:id>ID11234</xro:id>
        <xro:userId>EE1234567890</xro:userId>
        <xro:protocolVersion>4.0</xro:protocolVersion>
    </soapenv:Header>
    <soapenv:Body><ts1:format1_plainResponse><ts1:key1>content1</ts1:key1></ts1:format1_plainResponse></soapenv:Body>
</soapenv:Envelope>
```

# works
