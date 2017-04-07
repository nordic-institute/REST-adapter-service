<%--

    The MIT License
    Copyright © 2017 Population Register Centre (VRK)

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.

--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title></title>
    </head>
    <body>
        <h3>REST Gateway</h3>
        <p>REST Gateway on <a href="https://github.com/educloudalliance/xroad-rest-gateway" target="new">GitHub</a>.</p>
        <p><b>Consumer</b></p>
        <p>Consumer endpoint: <a href="${pageContext.request.requestURL}Consumer">${pageContext.request.requestURL}Consumer</a></p>
        <div>
            Properties:
            <ul>
                <li>WEB-INF/classes/consumer-gateway.properties</li>
                <li>WEB-INF/classes/consumers.properties</li>
            </ul>
        </div>
        <p><b>Provider</b></p>
        <p>Provider endpoint: <a href="${pageContext.request.requestURL}Provider">${pageContext.request.requestURL}Provider</a></p>
        <p>Provider WSDL: <a href="${pageContext.request.requestURL}Provider?wsdl">${pageContext.request.requestURL}Provider?wsdl</a></p>
        <p>WSDL path: <i>WEB-INF/classes/provider-gateway.wsdl</i></p>
        <div>
            Properties:
            <ul>
                <li>WEB-INF/classes/provider-gateway.properties</li>
                <li>WEB-INF/classes/providers.properties</li>
            </ul>
        </div>       
    </body>
</html>