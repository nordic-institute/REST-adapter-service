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
package fi.vrk.xroad.restadapterservice.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * This filter is responsible of filtering each request to "Consumer/*" URI. The
 * part after "Consumer/" is removed and stored in an attribute. The request is
 * then redirected to ConsumerEndpoint servlet that takes cares of processing
 * it.
 *
 * @author Petteri Kivimäki
 */
@Slf4j
public class ConsumerURIFilter implements Filter {

    @Override
    public void init(FilterConfig fc) throws ServletException {
        log.info("Consumer URI filter initialized.");
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain fc) throws IOException, ServletException {
        log.info("***** filter processing request");
        HttpServletRequest request = (HttpServletRequest) req;
        String servletName = "Consumer";
        String oldURI = request.getRequestURI().substring(request.getContextPath().length() + 1);
        log.debug("Incoming request : \"{}\"", oldURI);

        if (oldURI.length() > servletName.length()) {
            String resourcePath = oldURI.substring(oldURI.indexOf('/'));
            if (!"/".equals(resourcePath)) {
                // Path must end with "/"
                if (!resourcePath.endsWith("/")) {
                    resourcePath += "/";
                }
                log.debug("Resource path : \"{}\"", resourcePath);
                request.setAttribute("resourcePath", resourcePath);
            } else {
                log.trace("Found resource path \"{}\" is not valid.", resourcePath);
            }
        } else {
            log.trace("No resource path found.");
        }
        req.getRequestDispatcher("Consumer").forward(req, res);
    }

    @Override
    public void destroy() {
        // Nothing to do here.
    }
}
