/**
 * The MIT License
 * Copyright © 2017 Population Register Centre (VRK)
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
package fi.vrk.xroad.restadapterservice;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ApplicationTest {

    private static final String REST_ADAPTER_HAETOIMIJA_JSON_REQUEST = "{ \"Tunniste\": \"12345\" }";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestConfig.TestConsumerGateway testConsumerGateway;

    @Test
    public void testApplication() throws Exception {
        // check that the landing page (jsp) contains expected text
        assertThat(restTemplate.getForObject("/", String.class)).contains("REST Adapter Service");

        // do a dummy get request to consumer endpoint
        restTemplate.getForObject("/Consumer", String.class);

        // execute POST request to consumer endpoint
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        HttpEntity<String> entity = new HttpEntity<String>(REST_ADAPTER_HAETOIMIJA_JSON_REQUEST, headers);
        HttpEntity<String> response = restTemplate.exchange("/Consumer", HttpMethod.POST, entity, String.class);
        log.info("response: {}", response);
        log.info("response body: {}", response.getBody());
        log.info("response headers: {}", response.getHeaders());

        assertTrue(response.getHeaders().containsKey("Content-Type"));
        assertTrue(response.getHeaders().getContentType().equals(MediaType.APPLICATION_JSON_UTF8));
        assertTrue(response.getBody().contains("TekstiLisatiedot"));
        final String FULLRESPONSE_BODY = "kldhjaskjdash"; // TODO:
        assertEquals(FULLRESPONSE_BODY, response.getBody());


        log.info("testConsumerGateway: {}", testConsumerGateway);
        log.info("requests: {}", testConsumerGateway.getRequestBodies());
        log.info("4444444444443 testConsumerGateway: {}", testConsumerGateway);
    }


    @TestConfiguration
    public static class TestConfig {

        @Autowired
        TestConsumerGateway testConsumerGateway;

        @Bean
        public ServletRegistrationBean consumerGatewayBean() {
            log.info("test-consumerGatewayBean");
            log.info("testConsumerGateway: {}", testConsumerGateway);
            ServletRegistrationBean bean = new ServletRegistrationBean(
                    testConsumerGateway, "/Consumer/*");
            bean.setLoadOnStartup(1);
            return bean;
        }


        @Component
        public static class TestConsumerGateway extends ConsumerGateway {

            private List<String> requestBodies = new ArrayList<>();

            public List<String> getRequestBodies() {
                return requestBodies;
            }

            public TestConsumerGateway() {
                super();
                log.info("TestConsumerGateway");
            }

            @Override
            protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
                ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
                requestBodies.add(getRequestBody(request));
                log.info("test consumer gateway processing request....");
                log.info("request is: {}", request);
                super.processRequest(wrappedRequest, response);
            }

            private String getRequestBody(HttpServletRequest request) throws IOException {
                StringBuilder buffer = new StringBuilder();
                BufferedReader reader = request.getReader();
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                return buffer.toString();
            }
        }
    }

}