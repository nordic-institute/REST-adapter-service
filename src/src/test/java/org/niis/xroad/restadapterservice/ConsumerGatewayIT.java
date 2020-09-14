/*
 * The MIT License
 * Copyright © 2018 Nordic Institute for Interoperability Solutions (NIIS)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.niis.xroad.restadapterservice;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Condition;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.niis.xrd4j.rest.ClientResponse;
import org.niis.xrd4j.rest.client.RESTClient;
import org.niis.xrd4j.rest.client.RESTClientFactory;
import org.niis.xroad.restadapterservice.util.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.*;

import org.w3c.dom.Node;
import org.xmlunit.assertj.XmlAssert;
/**
 * This class contains integrations tests for REST Gateway.
 *
 * @author Petteri Kivimäki
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class ConsumerGatewayIT {

    private static final String CONTENT_TYPE_XML = Constants.TEXT_XML + ";" + Constants.CHARSET_UTF8;
    private static final String CONTENT_TYPE_JSON = Constants.APPLICATION_JSON + ";" + Constants.CHARSET_UTF8;
    private final Map<Integer, String> urls = new HashMap<>();
    private final Map<Integer, Map<String, List<String>>> urlParams = new HashMap<>();

    @LocalServerPort
    private int port;

    @Value("#{servletContext.contextPath}")
    private String servletContextPath;

    @Before
    public void setUp() {
        String buildPath = servletContextPath;
        String baseUrl = "http://localhost:" + port + "/" + buildPath + "/Consumer/";

        // Set up test case 1
        urls.put(1, baseUrl + "www.hel.fi/palvelukarttaws/rest/v2/organization/");
        Map<String, List<String>> caseParams = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("1010");
        caseParams.put(Constants.PARAM_RESOURCE_ID, values);
        this.urlParams.put(1, caseParams);

        // Set up test case 2
        urls.put(2, baseUrl + "www.hel.fi/palvelukarttaws/rest/v2/organization/");
        this.urlParams.put(2, new HashMap<String, List<String>>());

        // Set up test case 3
        urls.put(3, baseUrl + "api.finto.fi/rest/v1/search/");
        caseParams = new HashMap<>();
        values = new ArrayList<>();
        values.add("cat");
        caseParams.put("query", values);
        values = new ArrayList<>();
        values.add("en");
        caseParams.put("lang", values);
        values = new ArrayList<>();
        values.add("yso");
        caseParams.put("vocab", values);
        this.urlParams.put(3, caseParams);

        // Set up test case 4
        urls.put(4, baseUrl + "api.kirjastot.fi/v3/organisation/");
        caseParams = new HashMap<>();
        values = new ArrayList<>();
        values.add("kallio");
        caseParams.put("name", values);
        values = new ArrayList<>();
        values.add("helsinki");
        caseParams.put("city.name", values);
        this.urlParams.put(4, caseParams);
    }

    /**
     * REST API for City of Helsinki Service Map - List of Organizations - JSON
     */
    @Test
    public void testConsumerGateway1Json() {
        String result = "{\"data_source_url\":\"www.liikuntapaikat.fi\",\"name_fi\":\"Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi\",\"name_sv\":\"Jyväskylä universitet, LIPAS Liikuntapaikat.fi\",\"id\":1010,\"name_en\":\"University of Jyväskylä, LIPAS Liikuntapaikat.fi\"}";
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.HTTP_HEADER_ACCEPT, Constants.APPLICATION_JSON);
        sendData(this.urls.get(1), "get", this.urlParams.get(1), headers, result, CONTENT_TYPE_JSON);
    }

    /**
     * REST API for City of Helsinki Service Map - List of Organizations - XML
     */
    @Test
    public void testConsumerGateway1Xml() throws Exception {
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<ts1:getOrganizationResponse xmlns:ts1=\"http://x-road.global/producer\">\n" +
                "    <ts1:data_source_url>www.liikuntapaikat.fi</ts1:data_source_url>\n" +
                "    <ts1:name_fi>Jyväskylän yliopisto, LIPAS Liikuntapaikat.fi</ts1:name_fi>\n" +
                "    <ts1:name_sv>Jyväskylä universitet, LIPAS Liikuntapaikat.fi</ts1:name_sv>\n" +
                "    <ts1:id>1010</ts1:id>\n" +
                "    <ts1:name_en>University of Jyväskylä, LIPAS Liikuntapaikat.fi</ts1:name_en>\n" +
                "</ts1:getOrganizationResponse>";

        ClientResponse restResponse = sendData(this.urls.get(1), "get", this.urlParams.get(1), new HashMap<String, String>());
        String data = restResponse.getData();
        log.debug("data from service: {}", data);
        assertEquals(CONTENT_TYPE_XML, restResponse.getContentType());

        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(data, expectedResult);
        log.debug("diff: {}", diff);
        // diff is "similar" (not identical) even if namespace prefixes and element ordering differ
        assertTrue(diff.similar());
    }

    /**
     * REST API for City of Helsinki Service Map - Single Organization - JSON
     */
    @Test
    public void testConsumerGateway2Json() {
        ClientResponse restResponse = sendJsonGet(2);
        String data = restResponse.getData();
        assertThat(data, hasJsonPath("$[*].name_fi", hasItem("Helsingin kaupunki")));
        assertThat(data, hasJsonPath("$[*].name_fi", hasSize(greaterThan(1))));
        assertEquals(CONTENT_TYPE_JSON, restResponse.getContentType());
    }

    /**
     * REST API for City of Helsinki Service Map - Single Organization - XML
     */
    @Test
    public void testConsumerGateway2Xml() {
        ClientResponse restResponse = sendXmlGet(2);
        String data = restResponse.getData();
        assertEquals(CONTENT_TYPE_XML, restResponse.getContentType());
        HashMap<String, String> prefix2Uri = new HashMap<String, String>();
        prefix2Uri.put("ts1", "http://x-road.global/producer");
        XmlAssert.assertThat(data)
                .withNamespaceContext(prefix2Uri)
                .nodesByXPath("//ts1:getOrganizationListResponse/ts1:array/ts1:name_fi")
                .exist()
                .areExactly(1, new Condition<Node>() {
                    @Override
                    public boolean matches(Node node) {
                            return node != null &&
                                    node.getTextContent() != null &&
                                    node.getTextContent().equals("Helsingin kaupunki");
                    }
                });

        XmlAssert.assertThat(data)
                .withNamespaceContext(prefix2Uri)
                .valueByXPath("count(//ts1:getOrganizationListResponse/ts1:array)")
                .asInt().isGreaterThan(1);
    }

    /**
     * Finto : Finnish Thesaurus and Ontology Service - Search - JSON
     */
    @Test
    public void testConsumerGateway3Json() {
        ClientResponse restResponse = sendJsonGet(3);
        String data = restResponse.getData();
        assertThat(data, hasJsonPath("$.results.vocab", equalTo("yso")));
        assertEquals(CONTENT_TYPE_JSON, restResponse.getContentType());
    }

    /**
     * Finto : Finnish Thesaurus and Ontology Service - Search - XML
     */
    @Test
    public void testConsumerGateway3Xml() {
        ClientResponse restResponse = sendXmlGet(3);
        String data = restResponse.getData();
        assertEquals(CONTENT_TYPE_XML, restResponse.getContentType());
        HashMap<String, String> prefix2Uri = new HashMap<>();
        prefix2Uri.put("ts1", "http://x-road.global/producer");
        XmlAssert.assertThat(data)
                .withNamespaceContext(prefix2Uri)
                .valueByXPath("//ts1:fintoServiceResponse/ts1:results/ts1:vocab")
                .isEqualTo("yso");
    }

    /**
     * Finnish Library Directory - JSON - N.B.! Response contains cyrillic
     * characters.
     */
    @Test
    public void testConsumerGateway4Json() {
        ClientResponse restResponse = sendJsonGet(4);
        String data = restResponse.getData();
        assertThat(data, hasJsonPath("$.result.items.organisation.name.value[*].content", hasItem("Kallion kirjasto")));
        assertEquals(CONTENT_TYPE_JSON, restResponse.getContentType());
    }

    /**
     * Finnish Library Directory - XML - N.B.! Response contains cyrillic
     * characters.
     */
    @Test
    public void testConsumerGateway4Xml() {
        ClientResponse restResponse = sendXmlGet(4);
        String data = restResponse.getData();
        assertEquals(CONTENT_TYPE_XML, restResponse.getContentType());

        XmlAssert.assertThat(data)
                .nodesByXPath("//result/items/organisation/name/value")
                .exist()
                .areExactly(1, new Condition<Node>() {
                    @Override
                    public boolean matches(Node node) {
                        return node != null &&
                                node.getTextContent() != null &&
                                node.getTextContent().equals("Kallio Library");
                    }
                });

        XmlAssert.assertThat(data)
                .valueByXPath("count(//result/items/organisation/name/value)")
                .asInt().isGreaterThan(1);
    }

    /**
     * Execute request, and assert that response is expected
     * @param url
     * @param verb
     * @param urlParams
     * @param headers
     * @param expectedResponse expected response
     * @param expectedContentType expected response content type
     */
    private void sendData(String url, String verb, Map<String, List<String>> urlParams, Map<String, String> headers, String expectedResponse, String expectedContentType) {
        ClientResponse restResponse = sendData(url, verb, urlParams, headers);

        String data = restResponse.getData();

        assertEquals(expectedResponse, new String(data.getBytes(), StandardCharsets.UTF_8));
        assertEquals(expectedContentType, restResponse.getContentType());
    }

    /**
     * Execute request, and return ClientResponse
     * @param url
     * @param verb
     * @param urlParams
     * @param headers
     * @return
     */
    private ClientResponse sendData(String url, String verb, Map<String,
            List<String>> urlParams, Map<String, String> headers) {
        RESTClient restClient = RESTClientFactory.createRESTClient(verb);
        // Send request to the service endpoint
        System.out.println("sending request from test to: " + url);
        ClientResponse restResponse = restClient.send(url, "", urlParams, headers);
        return restResponse;
    }

    /**
     * Execute get request with parameters from given index
     */
    private ClientResponse sendXmlGet(int index) {
        return sendData(this.urls.get(index), "get", this.urlParams.get(index), new HashMap<String, String>());
    }


    /**
     * Execute json get request with parameters from given index
     * @param index
     * @return
     */
    private ClientResponse sendJsonGet(int index) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.HTTP_HEADER_ACCEPT, Constants.APPLICATION_JSON);
        return sendData(this.urls.get(index), "get", this.urlParams.get(index), headers);
    }


}
