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
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.ContentCachingRequestWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
public class ApplicationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TestConfig.TestConsumerGateway testConsumerGateway;

    @Test
    public void testApplication() throws Exception {
        String url = "http://localhost:" + port + "/rest-adapter-service/";
        log.info("!!!!! executing request to url {}", url);
        assertThat(restTemplate.getForObject(url, String.class)).contains("REST Adapter Service");
        url = "http://localhost:" + port + "/rest-adapter-service/Consumer";
        log.info("!!!!! executing request to url {}", url);
        restTemplate.getForObject(url, String.class);
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
                requestBodies.add("foorequest");
                requestBodies.add(getRequestBody(request));
                log.info("test consumer gateway processing request....");
                log.info("request is: {}", request);
                log.info("testConsumerGateway: {}", this);
                log.info("added stuff to requestBodies: {}", requestBodies);

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