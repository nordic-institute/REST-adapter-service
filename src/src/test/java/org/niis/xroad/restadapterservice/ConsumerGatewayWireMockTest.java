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

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.RequestPattern;
import com.github.tomakehurst.wiremock.verification.FindRequestsResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.niis.xroad.restadapterservice.util.Constants;
import lombok.extern.slf4j.Slf4j;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.ServerSocket;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Disabled
@SpringBootTest
@Slf4j
public class ConsumerGatewayWireMockTest {

    private WireMockServer wireMockServer;
    private int wireMockPort = findAvailableTcpPort();

    @BeforeEach
    public void setUp() throws Exception {
        // set up mock http server for ss
        wireMockServer = new WireMockServer(new WireMockConfiguration().port(wireMockPort));
        wireMockServer.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        wireMockServer.stop();
    }

    @Disabled
    @Test
    public void testJsonConversion() throws Exception {
        String json = readFile("consumer-gw-test-request.json");
        String xml = readFile("consumer-gw-test-response.xml");

        // start WireMock, which simulates the security server
        wireMockServer.stubFor(post(urlEqualTo("/some/thing"))
                .willReturn(aResponse().withHeader("Content-Type", "text/xml;charset=UTF-8\n").withBody(xml)));

        // prepare TestConsumerGateway, which overrides the config
        TestConsumerGateway testConsumerGateway = new TestConsumerGateway();
        Properties consumerGatewayProperties = new ConsumerGatewayPropertiesBuilder()
                .clientId("FI.GOV.1945065-0.REST-GW-CONSUMERS")
                .securityServerUrl("http://localhost:" + wireMockPort + "/some/thing")
                .enableServiceCallsByServiceId(true).useWrappers(false).build();
        Properties endpointProperties = new EndpointPropertiesBuilder(0).id("FI.GOV.1002.SS1.sendMessage.v1")
                .path("/sendMessage/").verb("post").serializeNamespace("http://com.example/some-namespace")
                .deserializeNamespace("http://com.example/some-namespace").serializeNamespacePrefix("some-ns")
                .convertPost(true).build();
        testConsumerGateway.setTestGatewayProperties(
                new ConsumerGateway.GatewayProperties(endpointProperties, consumerGatewayProperties));
        testConsumerGateway.init();

        // build a request that represents a call to rest-adapter which results in JSON
        // -> XML
        // conversion
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setMethod("POST");
        request.setRequestURI("http://localhost:" + wireMockPort + "/rest-adapter-service-0.0.12-SNAPSHOT/Consumer");
        request.setContextPath("rest-adapter-service-0.0.12-SNAPSHOT");
        request.addHeader("Accept", "application/json");
        request.addHeader(Constants.XRD_HEADER_MESSAGE_ID, "predefined-message-id");
        request.setAttribute("resourcePath", "/sendMessage/");
        request.setContent(json.getBytes());

        // call rest adapter
        MockHttpServletResponse response = new MockHttpServletResponse();
        testConsumerGateway.doPost(request, response);

        // assert that 1) wireMock received correct XML 2) we received correct JSON back

        // 1
        String requestBodyFromWireMock = findOneRequestFromWireMock();
        String expectedRequestXml = readFile("consumer-gw-test-request-expected.xml");
        ;
        log.debug("request received by wiremock: {}", requestBodyFromWireMock);
        log.debug("expected result xml: {}", expectedRequestXml);
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = new Diff(requestBodyFromWireMock, expectedRequestXml);
        log.debug("diff: {}", diff);
        // diff is "similar" (not identical) even if namespace prefixes and element
        // ordering differ
        assertTrue(diff.similar());

        // 2
        String expectedResponseJson = readFile("consumer-gw-test-response-expected.json");
        ;
        String actualResponseJson = response.getContentAsString();
        log.debug("json response: {}", actualResponseJson);
        log.debug("expected json response: {}", expectedResponseJson);
        JSONAssert.assertEquals(expectedResponseJson, actualResponseJson, JSONCompareMode.NON_EXTENSIBLE);
    }

    private String findOneRequestFromWireMock() {
        FindRequestsResult requestsResult = wireMockServer.findRequestsMatching(RequestPattern.everything());

        log.debug("WireMock received requests:");
        log.debug("assertEquals expects exactly 1 call to wiremock");
        assertEquals(1, requestsResult.getRequests().size());
        return requestsResult.getRequests().get(0).getBodyAsString();
    }

    private String readFile(String filename) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(ClassLoader.getSystemResource(filename).toURI())), "UTF-8");
    }

    private int findAvailableTcpPort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to find an available TCP port", e);
        }
    }

    private class TestConsumerGateway extends ConsumerGateway {
        private GatewayProperties testGatewayProperties;

        public void setTestGatewayProperties(GatewayProperties testGatewayProperties) {
            this.testGatewayProperties = testGatewayProperties;
        }

        @Override
        protected GatewayProperties readGatewayProperties() {
            return testGatewayProperties;
        }
    }

    public static class ConsumerGatewayPropertiesBuilder {
        private Properties properties = new Properties();

        private ConsumerGatewayPropertiesBuilder addProperty(String name, String value) {
            properties.setProperty(name, value);
            return this;
        }

        public ConsumerGatewayPropertiesBuilder clientId(String s) {
            return addProperty(Constants.CONSUMER_PROPS_ID_CLIENT, s);
        }

        public ConsumerGatewayPropertiesBuilder securityServerUrl(String s) {
            return addProperty(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL, s);
        }

        public ConsumerGatewayPropertiesBuilder enableServiceCallsByServiceId(boolean b) {
            return addProperty(Constants.CONSUMER_PROPS_SVC_CALLS_BY_XRD_SVC_ID_ENABLED, String.valueOf(b));
        }

        public ConsumerGatewayPropertiesBuilder useWrappers(boolean b) {
            return addProperty(Constants.ENDPOINT_PROPS_WRAPPERS, String.valueOf(b));
        }

        public Properties build() {
            return (Properties) properties.clone();
        }
    }

    public static class EndpointPropertiesBuilder {
        private Properties properties = new Properties();
        private int index;

        public EndpointPropertiesBuilder(int index) {
            this.index = index;
        }

        private EndpointPropertiesBuilder addProperty(String name, String value) {
            properties.setProperty(index + "." + name, value);
            return this;
        }

        public EndpointPropertiesBuilder id(String s) {
            return addProperty(Constants.ENDPOINT_PROPS_ID, s);
        }

        public EndpointPropertiesBuilder path(String s) {
            return addProperty(Constants.CONSUMER_PROPS_PATH, s);
        }

        public EndpointPropertiesBuilder verb(String s) {
            return addProperty(Constants.ENDPOINT_PROPS_VERB, s);
        }

        public EndpointPropertiesBuilder serializeNamespace(String s) {
            return addProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, s);
        }

        public EndpointPropertiesBuilder deserializeNamespace(String s) {
            return addProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, s);
        }

        public EndpointPropertiesBuilder serializeNamespacePrefix(String s) {
            return addProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE, s);
        }

        public EndpointPropertiesBuilder convertPost(boolean b) {
            return addProperty(Constants.CONSUMER_PROPS_CONVERT_POST, String.valueOf(b));
        }

        public Properties build() {
            return (Properties) properties.clone();
        }
    }

}
