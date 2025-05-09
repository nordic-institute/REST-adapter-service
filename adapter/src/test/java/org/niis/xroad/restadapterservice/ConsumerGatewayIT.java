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

import org.niis.xrd4j.rest.ClientResponse;
import org.niis.xrd4j.rest.client.RESTClient;
import org.niis.xrd4j.rest.client.RESTClientFactory;
import org.niis.xroad.restadapterservice.util.Constants;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Condition;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.w3c.dom.Node;
import org.xmlunit.assertj.XmlAssert;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * This class contains integrations tests for REST Gateway.
 *
 * @author Petteri Kivimäki
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Slf4j
public class ConsumerGatewayIT {

    private static final String CONTENT_TYPE_XML = Constants.TEXT_XML + ";" + Constants.CHARSET_UTF8;
    private static final String CONTENT_TYPE_JSON = Constants.APPLICATION_JSON + ";" + Constants.CHARSET_UTF8;

    private static final int TEST_CASE_1 = 1;
    private static final int TEST_CASE_2 = 2;
    private static final int TEST_CASE_3 = 3;
    private static final int TEST_CASE_4 = 4;

    private final Map<Integer, String> urls = new HashMap<>();
    private final Map<Integer, Map<String, List<String>>> urlParams = new HashMap<>();

    @Value("${local.server.port}")
    private int port;

    @Value("#{servletContext.contextPath}")
    private String servletContextPath;

    @BeforeEach
    public void setUp() {
        String buildPath = servletContextPath;
        String baseUrl = "http://localhost:" + port + buildPath + "/Consumer/";


        // Set up test case 1
        urls.put(TEST_CASE_1, baseUrl + "www.hel.fi/palvelukarttaws/rest/v4/department/");
        Map<String, List<String>> caseParams = new HashMap<>();
        List<String> values = new ArrayList<>();
        values.add("cc70d1d8-3ca7-416a-9ea7-27e6b7ce58a8");
        caseParams.put(Constants.PARAM_RESOURCE_ID, values);
        this.urlParams.put(TEST_CASE_1, caseParams);

        // Set up test case 2
        urls.put(TEST_CASE_2, baseUrl + "www.hel.fi/palvelukarttaws/rest/v4/organization/");
        this.urlParams.put(TEST_CASE_2, new HashMap<String, List<String>>());

        // Set up test case 3
        urls.put(TEST_CASE_3, baseUrl + "api.finto.fi/rest/v1/search/");
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
        this.urlParams.put(TEST_CASE_3, caseParams);

        // Set up test case 4
        urls.put(TEST_CASE_4, baseUrl + "api.kirjastot.fi/v3/organisation/");
        caseParams = new HashMap<>();
        values = new ArrayList<>();
        values.add("kallio");
        caseParams.put("name", values);
        values = new ArrayList<>();
        values.add("helsinki");
        caseParams.put("city.name", values);
        this.urlParams.put(TEST_CASE_4, caseParams);
    }

    /**
     * REST API for City of Helsinki Service Map - List of Organizations - JSON
     */
    @Test
    public void testConsumerGateway1Json() throws Exception {
        String json = readFile("consumer-IT-expected-response.json");
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.HTTP_HEADER_ACCEPT, Constants.APPLICATION_JSON);
        sendData(this.urls.get(TEST_CASE_1), "get", this.urlParams.get(TEST_CASE_1), headers, json, CONTENT_TYPE_JSON);
    }

    /**
     * REST API for City of Helsinki Service Map - List of Organizations - XML
     */
    @Test
    public void testConsumerGateway1Xml() throws Exception {
        String expectedResult = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>"
                + "<ts1:getOrganizationResponse xmlns:ts1=\"http://x-road.global/producer\">"
                + "    <ts1:abbr_fi>Kasko</ts1:abbr_fi>"
                + "    <ts1:www_en>https://www.hel.fi/en/decision-making/city-organization/divisions/education-division</ts1:www_en>"
                + "    <ts1:municipality_code>91</ts1:municipality_code>"
                + "    <ts1:oid/>"
                + "    <ts1:name_fi>Kasvatuksen ja koulutuksen toimiala</ts1:name_fi>"
                + "    <ts1:name_sv>Fostrans- och utbildningssektorn</ts1:name_sv>"
                + "    <ts1:phone>+358 9 310 8600</ts1:phone>"
                + "    <ts1:parent_id>83e74666-0836-4c1d-948a-4b34a8b90301</ts1:parent_id>"
                + "    <ts1:org_id>83e74666-0836-4c1d-948a-4b34a8b90301</ts1:org_id>"
                + "    <ts1:organization_type>MUNICIPALITY</ts1:organization_type>"
                + "    <ts1:id>cc70d1d8-3ca7-416a-9ea7-27e6b7ce58a8</ts1:id>"
                + "    <ts1:business_id>0201256-6</ts1:business_id>"
                + "    <ts1:email>neuvonta.opetusvirasto@hel.fi</ts1:email>"
                + "    <ts1:www_fi>https://www.hel.fi/fi/paatoksenteko-ja-hallinto/kaupungin-organisaatio/toimialat/kasvatuksen-ja-koulutuksen-toimiala</ts1:www_fi>"
                + "    <ts1:www_sv>https://www.hel.fi/sv/beslutsfattande-och-forvaltning/stadens-organisation/sektorer/fostrans-och-utbildningssektorn</ts1:www_sv>"
                + "    <ts1:hierarchy_level>1</ts1:hierarchy_level>"
                + "    <ts1:name_en>Education Division</ts1:name_en>"
                + "</ts1:getOrganizationResponse>";

        ClientResponse restResponse = sendData(this.urls.get(TEST_CASE_1), "get", this.urlParams.get(1),
                new HashMap<String, String>());
        String data = restResponse.getData();
        log.debug("data from service: {}", data);
        assertEquals(CONTENT_TYPE_XML, restResponse.getContentType());

        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(data, expectedResult);
        log.debug("diff: {}", diff);
        // diff is "similar" (not identical) even if namespace prefixes and element
        // ordering differ
        assertTrue(diff.similar());
    }

    /**
     * REST API for City of Helsinki Service Map - Single Organization - JSON
     */
    @Test
    public void testConsumerGateway2Json() {
        ClientResponse restResponse = sendJsonGet(TEST_CASE_2);
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
        ClientResponse restResponse = sendXmlGet(TEST_CASE_2);
        String data = restResponse.getData();
        assertEquals(CONTENT_TYPE_XML, restResponse.getContentType());
        HashMap<String, String> prefix2Uri = new HashMap<String, String>();
        prefix2Uri.put("ts1", "http://x-road.global/producer");
        XmlAssert.assertThat(data).withNamespaceContext(prefix2Uri)
                .nodesByXPath("//ts1:getOrganizationListResponse/ts1:array/ts1:name_fi").exist()
                .areExactly(1, new Condition<Node>() {
                    @Override
                    public boolean matches(Node node) {
                        return node != null && node.getTextContent() != null
                                && node.getTextContent().equals("Helsingin kaupunki");
                    }
                });

        XmlAssert.assertThat(data).withNamespaceContext(prefix2Uri)
                .valueByXPath("count(//ts1:getOrganizationListResponse/ts1:array)").asInt().isGreaterThan(1);
    }

    /**
     * Finto : Finnish Thesaurus and Ontology Service - Search - JSON
     */
    @Test
    public void testConsumerGateway3Json() {
        ClientResponse restResponse = sendJsonGet(TEST_CASE_3);
        String data = restResponse.getData();
        assertThat(data, hasJsonPath("$.results.vocab", equalTo("yso")));
        assertEquals(CONTENT_TYPE_JSON, restResponse.getContentType());
    }

    /**
     * Finto : Finnish Thesaurus and Ontology Service - Search - XML
     */
    @Test
    public void testConsumerGateway3Xml() {
        ClientResponse restResponse = sendXmlGet(TEST_CASE_3);
        String data = restResponse.getData();
        assertEquals(CONTENT_TYPE_XML, restResponse.getContentType());
        HashMap<String, String> prefix2Uri = new HashMap<>();
        prefix2Uri.put("ts1", "http://x-road.global/producer");
        XmlAssert.assertThat(data).withNamespaceContext(prefix2Uri)
                .valueByXPath("/ts1:fintoServiceResponse/ts1:results/ts1:vocab").isEqualTo("yso");
    }

    /**
     * Finnish Library Directory - JSON - N.B.! Response contains cyrillic
     * characters.
     */

    @Test
    public void testConsumerGateway4Json() {
        ClientResponse restResponse = sendJsonGet(TEST_CASE_4);
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
        ClientResponse restResponse = sendXmlGet(TEST_CASE_4);
        String data = restResponse.getData();
        assertEquals(CONTENT_TYPE_XML, restResponse.getContentType());

        XmlAssert.assertThat(data).nodesByXPath("//result/items/organisation/name/value").exist().areExactly(1,
                new Condition<Node>() {
                    @Override
                    public boolean matches(Node node) {
                        return node != null && node.getTextContent() != null
                                && node.getTextContent().equals("Kallio Library");
                    }
                });

        XmlAssert.assertThat(data).valueByXPath("count(//result/items/organisation/name/value)").asInt()
                .isGreaterThan(1);
    }

    /**
     * Execute request, and assert that response is expected
     *
     * @param url
     * @param verb
     * @param testCaseUrlParams
     * @param headers
     * @param expectedResponse    expected response
     * @param expectedContentType expected response content type
     */
    private void sendData(String url, String verb, Map<String, List<String>> testCaseUrlParams, Map<String, String> headers,
                          String expectedResponse, String expectedContentType) {
        ClientResponse restResponse = sendData(url, verb, testCaseUrlParams, headers);

        String data = restResponse.getData();

        assertEquals(expectedResponse, new String(data.getBytes(), StandardCharsets.UTF_8));
        assertEquals(expectedContentType, restResponse.getContentType());
    }

    /**
     * Execute request, and return ClientResponse
     *
     * @param url
     * @param verb
     * @param testCaseUrlParams
     * @param headers
     * @return
     */
    private ClientResponse sendData(String url, String verb, Map<String, List<String>> testCaseUrlParams,
                                    Map<String, String> headers) {
        RESTClient restClient = RESTClientFactory.createRESTClient(verb);
        // Send request to the service endpoint
        System.out.println("sending request from test to: " + url);
        ClientResponse restResponse = restClient.send(url, "", testCaseUrlParams, headers);
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
     *
     * @param index
     * @return
     */
    private ClientResponse sendJsonGet(int index) {
        Map<String, String> headers = new HashMap<>();
        headers.put(Constants.HTTP_HEADER_ACCEPT, Constants.APPLICATION_JSON);
        return sendData(this.urls.get(index), "get", this.urlParams.get(index), headers);
    }

    private String readFile(String filename) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(filename).toURI())), "UTF-8");
    }

}
