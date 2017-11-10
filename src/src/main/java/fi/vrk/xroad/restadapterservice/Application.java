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

import fi.vrk.xroad.restadapterservice.filter.ConsumerURIFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.DispatcherType;
import java.util.Arrays;
import java.util.EnumSet;

@Configuration
@Slf4j
@SpringBootApplication
public class Application extends SpringBootServletInitializer {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean consumerGatewayBean() {
        log.info("consumerGatewayBean");
        ServletRegistrationBean bean = new ServletRegistrationBean(
                new ConsumerGateway(), "/Consumer/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean providerGatewayBean() {
        log.info("providerGatewayBean");
        ServletRegistrationBean bean = new ServletRegistrationBean(
                new ProviderGateway(), "/Provider");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean ConsumerURIFilterBean() {
        log.info("ConsumerURIFilterBean");
        FilterRegistrationBean bean = new FilterRegistrationBean();
        bean.setFilter(new ConsumerURIFilter());
        bean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        bean.setUrlPatterns(Arrays.asList("/Consumer/*"));
        return bean;
    }

}
