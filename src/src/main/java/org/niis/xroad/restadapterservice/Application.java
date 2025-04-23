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

import org.niis.xrd4j.server.AbstractAdapterServlet;
import org.niis.xroad.restadapterservice.filter.ConsumerURIFilter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServlet;

import java.util.Arrays;
import java.util.EnumSet;


/**
 * Rest adapter service application entry point
 */
@Configuration
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

    public static void main(String[] args) {

        SpringApplication.run(Application.class, args);
    }

    @Bean
    public ServletRegistrationBean consumerGatewayBean() {
        ServletRegistrationBean<ConsumerGateway> bean = new ServletRegistrationBean<ConsumerGateway>(
                new ConsumerGateway(), "/Consumer/*");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public ServletRegistrationBean providerGatewayBean() {
        ServletRegistrationBean<ProviderGateway> bean = new ServletRegistrationBean<ProviderGateway>(
                new ProviderGateway(), "/Provider");
        bean.setLoadOnStartup(1);
        return bean;
    }

    @Bean
    public FilterRegistrationBean consumerURIFilterBean() {
        FilterRegistrationBean<ConsumerURIFilter> bean = new FilterRegistrationBean();
        bean.setFilter(new ConsumerURIFilter());
        bean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST));
        bean.setUrlPatterns(Arrays.asList("/Consumer/*"));
        return bean;
    }
}
