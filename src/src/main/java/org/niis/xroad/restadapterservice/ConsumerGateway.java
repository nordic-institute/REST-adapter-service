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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.soap.*;
import org.niis.xrd4j.client.SOAPClient;
import org.niis.xrd4j.client.SOAPClientImpl;
import org.niis.xrd4j.client.deserializer.AbstractResponseDeserializer;
import org.niis.xrd4j.client.deserializer.ServiceResponseDeserializer;
import org.niis.xrd4j.client.serializer.AbstractServiceRequestSerializer;
import org.niis.xrd4j.client.serializer.ServiceRequestSerializer;
import org.niis.xrd4j.common.exception.XRd4JException;
import org.niis.xrd4j.common.message.ErrorMessage;
import org.niis.xrd4j.common.message.ServiceRequest;
import org.niis.xrd4j.common.message.ServiceResponse;
import org.niis.xrd4j.common.security.Decrypter;
import org.niis.xrd4j.common.security.Encrypter;
import org.niis.xrd4j.common.util.MessageHelper;
import org.niis.xrd4j.common.util.PropertiesUtil;
import org.niis.xrd4j.common.util.SOAPHelper;
import org.niis.xrd4j.rest.converter.JSONToXMLConverter;
import org.niis.xrd4j.rest.converter.XMLToJSONConverter;
import org.niis.xroad.restadapterservice.endpoint.ConsumerEndpoint;
import org.niis.xroad.restadapterservice.util.Constants;
import org.niis.xroad.restadapterservice.util.ConsumerGatewayUtil;
import org.niis.xroad.restadapterservice.util.RESTGatewayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.w3c.dom.Node.ELEMENT_NODE;


/**
 * This class implements a Servlet which functionality can be configured through
 * external properties files. This class implements a REST consumer gateway by
 * forwarding incoming requests to configured X-Road security server, and
 * returning the responses to the requesters. Requests and responses can be
 * converted from JSON to XML.
 *
 * @author Petteri Kivimäki
 */
public class ConsumerGateway extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ConsumerGateway.class);

    public static final String JSON_CONVERSION_WRAPPER_ELEMENT = "jsonWrapperProperty";
    private Properties props;
    private Map<String, ConsumerEndpoint> endpoints;
    private boolean serviceCallsByXRdServiceId;
    private Decrypter asymmetricDecrypter;
    private final Map<String, Encrypter> asymmetricEncrypterCache = new HashMap<>();
    private String publicKeyFile;
    private String publicKeyFilePassword;
    private int keyLength;

    /**
     * Reads properties (overridden in tests)
     *
     * @return
     */
    protected GatewayProperties readGatewayProperties() {
        log.debug("Reading Consumer and ConsumerGateway properties");
        String propertiesDirectory = RESTGatewayUtil.getPropertiesDirectory();
        Properties readEndpointProps;
        Properties readConsumerGatewayProps;
        if (propertiesDirectory != null) {
            readEndpointProps = PropertiesUtil.getInstance()
                    .load(propertiesDirectory + Constants.PROPERTIES_FILE_CONSUMERS, false);
            readConsumerGatewayProps = PropertiesUtil.getInstance()
                    .load(propertiesDirectory + Constants.PROPERTIES_FILE_CONSUMER_GATEWAY, false);
        } else {
            readEndpointProps = PropertiesUtil.getInstance().load(Constants.PROPERTIES_FILE_CONSUMERS);
            readConsumerGatewayProps = PropertiesUtil.getInstance().load(Constants.PROPERTIES_FILE_CONSUMER_GATEWAY);
        }
        return new GatewayProperties(readEndpointProps, readConsumerGatewayProps);
    }

    @Override
    public void init() throws ServletException {
        super.init();
        log.debug("Starting to initialize Consumer REST Gateway.");
        GatewayProperties gatewayProperties = readGatewayProperties();
        this.props = gatewayProperties.getConsumerGatewayProps();
        Properties endpointProps = gatewayProperties.getEndpointProps();

        log.debug("Setting Consumer and ConsumerGateway properties");
        String serviceCallsByXRdServiceIdStr = this.props
                .getProperty(Constants.CONSUMER_PROPS_SVC_CALLS_BY_XRD_SVC_ID_ENABLED);
        this.serviceCallsByXRdServiceId = serviceCallsByXRdServiceIdStr == null ? false
                : "true".equalsIgnoreCase(serviceCallsByXRdServiceIdStr);
        log.debug("Security server URL : \"{}\".", getSecurityServerUrl());
        log.debug("Default client id : \"{}\".", this.props.getProperty(Constants.CONSUMER_PROPS_ID_CLIENT));
        log.debug("Default namespace for incoming ServiceResponses : \"{}\".",
                this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE));
        log.debug("Default namespace for outgoing ServiceRequests : \"{}\".",
                this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE));
        log.debug("Default namespace prefix for outgoing ServiceRequests : \"{}\".",
                this.props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE));
        log.debug("Service calls by X-Road service id are enabled : {}.", this.serviceCallsByXRdServiceId);
        this.publicKeyFile = props.getProperty(Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE);
        this.publicKeyFilePassword = props.getProperty(Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE_PASSWORD);
        this.keyLength = RESTGatewayUtil.getKeyLength(props);
        log.debug("Symmetric key length : \"{}\".", this.keyLength);
        log.debug("Extracting individual consumers from properties");
        this.endpoints = ConsumerGatewayUtil.extractConsumers(endpointProps, this.props);
        // Check encryption properties. The method also sets the values of
        // asymmetricEncrypterCache.
        if (ConsumerGatewayUtil.checkEncryptionProperties(props, endpoints, this.asymmetricEncrypterCache)) {
            this.asymmetricDecrypter = RESTGatewayUtil.checkPrivateKey(props);
        }
    }

    /**
     * Processes requests for HTTP <code>GET</code>, <code>POST</code>,
     * <code>PUT</code> and <code>DELETE</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String responseStr;
        // Get resourcePath attribute
        String resourcePath = (String) request.getAttribute("resourcePath");
        // Get HTTP headers
        String userId = processUserId(getXRdHeader(request, Constants.XRD_HEADER_USER_ID));
        String messageId = processMessageId(getXRdHeader(request, Constants.XRD_HEADER_MESSAGE_ID));
        String namespace = getXRdHeader(request, Constants.XRD_HEADER_NAMESPACE_SERIALIZE);
        String prefix = getXRdHeader(request, Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE);
        String contentType = request.getHeader(Constants.HTTP_HEADER_CONTENT_TYPE);
        String acceptHeader = getXRdHeader(request, Constants.HTTP_HEADER_ACCEPT) == null ? Constants.TEXT_XML
                : getXRdHeader(request, Constants.HTTP_HEADER_ACCEPT);
        log.info("Request received. Method : \"{}\". Resource path : \"{}\".", request.getMethod(), resourcePath);

        // Check accept header
        String accept = processAcceptHeader(acceptHeader);
        // Set reponse content type according the accept header
        response.setContentType(accept);

        // Omit response namespace, if response is wanted in JSON
        boolean omitNamespace = accept.startsWith(Constants.APPLICATION_JSON);

        // Set userId and messageId to response
        response.addHeader(Constants.XRD_HEADER_USER_ID, userId);
        response.addHeader(Constants.XRD_HEADER_MESSAGE_ID, messageId);

        if (resourcePath == null) {
            // No resource path was defined -> return 404
            writeError404(response, accept);
            return;
        }

        // Build the service id for the incoming request
        String serviceId = request.getMethod() + " " + resourcePath;
        log.debug("Incoming service id to be looked for : \"{}\"", serviceId);
        // Try to find a configured endpoint matching the request's
        // service id
        ConsumerEndpoint endpoint = ConsumerGatewayUtil.findMatch(serviceId, endpoints);

        // If endpoint is null, try to use resourcePath as service id
        if (endpoint == null) {
            if (this.serviceCallsByXRdServiceId) {
                log.info("Endpoint is null, use resource path as service id. Resource path : \"{}\"", resourcePath);
                endpoint = ConsumerGatewayUtil.createUnconfiguredEndpoint(this.props, resourcePath);
            } else {
                log.info("Endpoint is null and service calls by X-Road service id are disabled. Nothing to do here.");
            }
        }
        // If endpoint is still null, return error message
        if (endpoint == null) {
            // No endpoint was found -> return 404
            writeError404(response, accept);
            // Quit processing
            return;
        }

        // Set namespace and prefix received from header, if not null or empty
        processNamespaceAndPrefix(endpoint, namespace, prefix);

        log.info("Starting to process \"{}\" service. X-Road id : \"{}\". Message id : \"{}\".", serviceId,
                endpoint.getServiceId(), messageId);
        try {
            // Create ServiceRequest object
            ServiceRequest<SOAPElement> serviceRequest = new ServiceRequest<>(endpoint.getConsumer(),
                    endpoint.getProducer(), messageId);
            // Set userId
            serviceRequest.setUserId(userId);
            // serviceRequest carries its payload as an SOAPElement
            SOAPElement containerElement = SOAPFactory.newInstance().createElement("container");
            serviceRequest.setRequestData(containerElement);

            // store request parameters in serviceRequest
            Map<String, String[]> params = filterRequestParameters(request.getParameterMap());
            for (Map.Entry<String, String[]> entry : params.entrySet()) {
                final String key = entry.getKey();
                for (String value : entry.getValue()) {
                    log.debug("Add parameter : \"{}\" -> \"{}\".", key, value);
                    containerElement.addChildElement(key).addTextNode(value);
                }
            }

            String requestBody = readRequestBody(request);
            if (endpoint.isConvertPost()) {
                // convert request body (JSON) into XML element and store end result inside
                // containerElement
                String xml = convertJsonToXml(requestBody);
                SOAPElement elementFromBody = SOAPHelper.xmlStrToSOAPElement(xml);
                SOAPHelper.moveChildren(elementFromBody, containerElement, true);
            }

            // Set request wrapper processing
            if (endpoint.isProcessingWrappers() != null) {
                serviceRequest.setProcessingWrappers(endpoint.isProcessingWrappers());
            }

            ServiceRequestSerializer serializer = getRequestSerializer(endpoint, requestBody, contentType);
            // Deserializer that converts the response from SOAP to XML/JSON
            ServiceResponseDeserializer deserializer = getResponseDeserializer(endpoint, omitNamespace);
            // SOAP client that makes the service call
            SOAPClient client = new SOAPClientImpl();
            log.info("Send request ({}) to the security server. URL : \"{}\".", messageId, getSecurityServerUrl());
            // Make the service call that returns the service response
            ServiceResponse serviceResponse = client.send(serviceRequest, getSecurityServerUrl(), serializer,
                    deserializer);
            log.info("Received response ({}) from the security server.", messageId);
            // Set response wrapper processing
            if (endpoint.isProcessingWrappers() != null) {
                serviceResponse.setProcessingWrappers(endpoint.isProcessingWrappers());
            }
            // Generate response message
            responseStr = handleResponse(response, serviceResponse);

            // Check if the URLs in the response should be rewritten
            // to point this servlet
            if (endpoint.isModifyUrl()) {
                // Get ConsumerGateway URL
                String servletUrl = getServletUrl(request);
                // Modify the response
                responseStr = ConsumerGatewayUtil.rewriteUrl(servletUrl, resourcePath, responseStr);
            }
            log.info("Processing \"{}\" service successfully completed. X-Road id : \"{}\". Message id : \"{}\".",
                    serviceId, endpoint.getServiceId(), messageId);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            log.error("Processing \"{}\" service failed. X-Road id : \"{}\". Message id : \"{}\".", serviceId,
                    endpoint.getServiceId(), messageId);
            // Internal server error -> return 500
            responseStr = generateError(Constants.ERROR_500, accept);
            response.setStatus(Constants.HTTP_INTERNAL_ERROR);
        }

        // Send response
        writeResponse(response, responseStr);
    }

    protected String getSecurityServerUrl() {
        return props.getProperty(Constants.CONSUMER_PROPS_SECURITY_SERVER_URL);
    }

    private void writeError404(HttpServletResponse response, String accept) {
        String responseStr;
        responseStr = generateError(Constants.ERROR_404, accept);
        response.setStatus(Constants.HTTP_NOT_FOUND);
        // Send response
        writeResponse(response, responseStr);
        // Quit processing
    }

    /**
     * Returns a new ServiceRequestSerializer that converts the request to SOAP. The
     * implementation of the ServiceRequestSerializer is decided based on the given
     * parameters.
     *
     * @param endpoint    ConsumerEndpoint that's processed using the serializer
     * @param requestBody request body that's being processed
     * @param contentType content type of the request
     * @return new ServiceRequestSerializer object
     * @throws XRd4JException
     */
    private ServiceRequestSerializer getRequestSerializer(ConsumerEndpoint endpoint, String requestBody,
                                                          String contentType) throws XRd4JException {
        // Type of the serializer depends on the encryption
        if (endpoint.isRequestEncrypted()) {
            log.debug("Endpoint requires that request is encrypted.");
            String providerId = endpoint.getProducer().toString();
            Encrypter asymmetricEncrypter;
            // Check if encrypter already exists in cache - it should as all
            // the encrypters are loaded during start up
            if (this.asymmetricEncrypterCache.containsKey(providerId)) {
                asymmetricEncrypter = RESTGatewayUtil.getEncrypter(this.publicKeyFile, this.publicKeyFilePassword,
                        providerId);
                log.trace("Asymmetric encrypter for provider \"{}\" loaded from cache.", providerId);
            } else {
                // Create new encrypter if it does not exist already for some reason
                asymmetricEncrypter = RESTGatewayUtil.getEncrypter(this.publicKeyFile, this.publicKeyFilePassword,
                        providerId);
                // Add new encrypter to the cache
                this.asymmetricEncrypterCache.put(providerId, asymmetricEncrypter);
                log.trace("Asymmetric encrypter for provider \"{}\" not found from cache. New ecrypter created.",
                        providerId);
            }
            if (asymmetricEncrypter == null) {
                throw new XRd4JException("No public key found when encryption is required.");
            }
            return new EncryptingRequestSerializer(endpoint.getResourceId(), requestBody, contentType,
                    asymmetricEncrypter, this.keyLength, endpoint.isConvertPost());
        } else {
            return new RequestSerializer(endpoint.getResourceId(), requestBody, contentType, endpoint.isConvertPost());
        }
    }

    /**
     * Returns a new ServiceResponseDeserializer that converts the response from
     * SOAP to XML/JSON. The implementation of the ServiceResponseDeserializer is
     * decided based on the given parameters.
     *
     * @param endpoint      ConsumerEndpoint that's processed using the deserializer
     * @param omitNamespace boolean value that tells if the response namespace
     *                      should be omitted by the deserializer
     * @return new ServiceResponseDeserializer object
     * @throws XRd4JException
     */
    private ServiceResponseDeserializer getResponseDeserializer(ConsumerEndpoint endpoint, boolean omitNamespace)
            throws XRd4JException {
        // Type of the serializer depends on the encryption
        if (endpoint.isResponseEncrypted()) {
            // If asymmetric decrypter is null, there's nothing to do
            if (this.asymmetricDecrypter == null) {
                throw new XRd4JException("No private key available when decryption is required.");
            }
            return new EncryptingResponseDeserializer(omitNamespace, this.asymmetricDecrypter);
        } else {
            // Deserializer that converts the response from SOAP to XML/JSON string
            return new ResponseDeserializer(omitNamespace);
        }
    }

    /**
     * Returns "anonymous" if the given user id is null. Otherwise returns the given
     * user id.
     *
     * @param userId user id to be checked
     * @return "anonymous" if the given user id is null; otherwise userId
     */
    private String processUserId(String userId) {
        // Set userId if null
        if (userId == null) {
            log.debug("\"{}\" header is null. Use \"anonymous\" as userId.", Constants.XRD_HEADER_USER_ID);
            return "anonymous";
        }
        return userId;
    }

    /**
     * Generates a unique identifier if the given message id is null. Otherwise
     * returns the given message id.
     *
     * @param messageId message id to be checked
     * @return unique identifier if the given message id is null; otherwise
     * messageId
     */
    private String processMessageId(String messageId) {
        // Set messageId if null
        if (messageId == null) {
            String id = MessageHelper.generateId();
            log.debug("\"{}\" header is null. Use auto-generated id \"{}\" instead.", Constants.XRD_HEADER_MESSAGE_ID,
                    id);
            return id;
        }
        return messageId;

    }

    /**
     * Checks namespace and prefix for null and empty, and sets them to endpoint if
     * a value is found.
     *
     * @param endpoint  ConsumerEndpoint object
     * @param namespace namespace HTTP header String
     * @param prefix    prefix HTTP header String
     */
    private void processNamespaceAndPrefix(ConsumerEndpoint endpoint, String namespace, String prefix) {
        // Set namespace received from header, if not null or empty
        if (!RESTGatewayUtil.isNullOrEmpty(namespace)) {
            endpoint.getProducer().setNamespaceUrl(namespace);
            log.debug("\"{}\" HTTP header found. Value : \"{}\".", Constants.XRD_HEADER_NAMESPACE_SERIALIZE, namespace);
        }
        // Set prefix received from header, if not null or empty
        if (!RESTGatewayUtil.isNullOrEmpty(prefix)) {
            endpoint.getProducer().setNamespacePrefix(prefix);
            log.debug("\"{}\" HTTP header found. Value : \"{}\".", Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE,
                    prefix);
        }
    }

    /**
     * Checks the value of the accept header and sets it to "text/xml" if no valid
     * value is found. UTF8 character set definition is added if missing.
     *
     * @param accept HTTP Accept header value from the request
     * @return sanitized Accept header value
     */
    private String processAcceptHeader(String accept) {
        // Accept header must be "text/xml" or "application/json"
        log.debug("Incoming accept header value : \"{}\"", accept);
        if (!accept.startsWith(Constants.TEXT_XML) && !accept.startsWith(Constants.APPLICATION_JSON)) {
            log.trace("Accept header value set to \"{}\".", Constants.TEXT_XML);
            return Constants.TEXT_XML + "; " + Constants.CHARSET_UTF8;
        }
        // Character set must be added to the accept header, if it's missing
        if (!accept.endsWith("8")) {
            return accept + "; " + Constants.CHARSET_UTF8;
        }
        return accept;
    }

    /**
     * Process the response and check it for error messages and attachments etc.,
     * and generate the response message string.
     *
     * @param response        HttpServletResponse object
     * @param serviceResponse ServiceResponse object
     * @return response message as a String
     */
    private String handleResponse(HttpServletResponse response, ServiceResponse serviceResponse) {
        String responseStr;
        // Check that response doesn't contain SOAP fault
        if (!serviceResponse.hasError()) {
            // Get the response that's now XML string. If the response has
            // attachments, the first attachment is returned. In case
            // of attachment, the response might not be XML. This is
            // handled later.
            responseStr = (String) serviceResponse.getResponseData();
        } else {
            // Error message detected
            log.debug("Received response contains SOAP fault.");
            responseStr = generateFault(serviceResponse.getErrorMessage());
        }
        // SOAP message doesn't have attachments
        if (!SOAPHelper.hasAttachments(serviceResponse.getSoapMessage())) {
            // Convert the response according to content type and remove
            // response tag if possible
            responseStr = handleResponseBody(response, responseStr);
        } else {
            // SOAP message has attachments. Use attachment's
            // content type.
            String attContentType = SOAPHelper.getAttachmentContentType(serviceResponse.getSoapMessage());
            response.setContentType(attContentType);
            log.debug("Use SOAP attachment as response message.");
        }
        return responseStr;
    }

    /**
     * Checks the content type and tries to convert the response accordingly. In
     * addition, the method tries to remove the response tag and its namespace
     * prefixes from the response string.
     *
     * @param response    HttpServletResponse object
     * @param responseStr response message as a String
     * @return modified response message as a String
     */
    private String handleResponseBody(HttpServletResponse response, String responseStr) {
        // If content type is JSON and the SOAP message doesn't have
        // attachments, the response must be converted
        if (response.getContentType().startsWith(Constants.APPLICATION_JSON)) {
            log.debug("Convert response from XML to JSON.");
            // Remove response tag and its namespace prefixes
            String tmp = ConsumerGatewayUtil.removeResponseTag(responseStr);
            return new XMLToJSONConverter().convert(tmp);
        } else if (response.getContentType().startsWith(Constants.TEXT_XML)) {
            // Remove response tag and its namespace prefixes
            String responseStrTemp = ConsumerGatewayUtil.removeResponseTag(responseStr);
            // Try to convert modified response to SOAP element
            if (SOAPHelper.xmlStrToSOAPElement(responseStrTemp) != null) {
                log.debug("Response tag was removed from the response string.");
                // If conversion succeeded response tag was only
                // a wrapper that can be removed
                return responseStrTemp;
            } else {
                log.debug("Response tag is the root element and cannot be removed.");
            }
        }
        return responseStr;
    }

    /**
     * Sends the response to the requester.
     *
     * @param response    HttpServletResponse object
     * @param responseStr response payload as a String
     */
    private static void writeResponse(HttpServletResponse response, String responseStr) {
        PrintWriter out = null;
        try {
            log.debug("Send response.");

            log.debug("Response content type : \"{}\".", response.getContentType());
            // Get writer
            out = response.getWriter();
            // Send response
            out.println(responseStr);
            log.trace("Consumer Gateway response : \"{}\"", responseStr);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (out != null) {
                out.close();
            }
            log.debug("Request was successfully processed.");
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>PUT</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>DELETE</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private static String generateError(String errorMsg, String contentType) {
        StringBuilder builder = new StringBuilder();
        if (contentType.startsWith(Constants.APPLICATION_JSON)) {
            builder.append("{\"error\":\"").append(errorMsg).append("\"}");
        } else {
            builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            builder.append("<error>").append(errorMsg).append("</error>");
        }
        return builder.toString();
    }

    private static String generateFault(ErrorMessage err) {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        builder.append("<error>");
        builder.append("<code>").append(err.getFaultCode()).append("</code>");
        builder.append("<string>").append(err.getFaultString()).append("</string>");
        if (err.getFaultActor() != null) {
            builder.append("<actor>").append(err.getFaultActor()).append("</actor>");
        } else {
            builder.append("<actor/>");
        }
        if (err.getDetail() != null) {
            builder.append("<detail>").append(err.getDetail()).append("</detail>");
        } else {
            builder.append("<detail/>");
        }
        builder.append("</error>");
        return builder.toString();
    }

    /**
     * Return the URL of this servlet.
     *
     * @param request HTTP servlet request
     * @return URL of this servlet
     */
    private static String getServletUrl(HttpServletRequest request) {
        return request.getScheme() + "://" + // "http" + "://
                request.getServerName() + // "myhost"
                ":" + // ":"
                request.getServerPort() + // "8080"
                request.getContextPath() + "/Consumer/";
    }

    /**
     * Checks if URL parameter (1) or HTTP header (2) with the given name exists and
     * returns its value. If no URL parameter or HTTP header is found, null is
     * returned. URL parameters are the primary source, and HTTP headers secondary
     * source.
     *
     * @param request HTTP request
     * @param header  name of the header
     * @return value of the header
     */
    private static String getXRdHeader(HttpServletRequest request, String header) {
        String headerValue = request.getParameter(header);
        if (headerValue != null && !headerValue.isEmpty()) {
            return headerValue;
        }
        return request.getHeader(header);
    }

    /**
     * Removes all the X-Road specific HTTP and SOAP headers from the request
     * parameters map. This method must be called before writing the parameters to
     * the SOAP request object.
     *
     * @param parameters HTTP request parameters map
     * @return filtered parameters map
     */
    private static Map<String, String[]> filterRequestParameters(Map<String, String[]> parameters) {
        // Request parameters map is unmodifiable so we need to copy it
        Map<String, String[]> params = new HashMap<>(parameters);
        // Remove X-Road headers
        params.remove(Constants.XRD_HEADER_USER_ID);
        params.remove(Constants.XRD_HEADER_MESSAGE_ID);
        params.remove(Constants.XRD_HEADER_NAMESPACE_SERIALIZE);
        params.remove(Constants.XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE);
        params.remove(Constants.HTTP_HEADER_ACCEPT);
        // Return copied parameters Map
        return params;
    }

    /**
     * Reads the request body from the request and returns it as a String.
     *
     * @param request HttpServletRequest that contains the request body
     * @return request body as a String or null
     */
    private static String readRequestBody(HttpServletRequest request) {
        try {
            // Read from request
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }

            return buffer.toString();
        } catch (Exception e) {
            log.error("Failed to read the request body from the request.", e);
        }
        return null;
    }

    /**
     * Serializes GET, POST, PUT and DELETE requests to SOAP.
     */
    private class RequestSerializer extends AbstractServiceRequestSerializer {

        protected final String resourceId;
        protected final String requestBody;
        protected final String contentType;
        protected final boolean convertPost;

        RequestSerializer(String resourceId, String requestBody, String contentType, boolean convertPost) {
            this.resourceId = resourceId;
            this.requestBody = requestBody;
            this.contentType = contentType;
            this.convertPost = convertPost;
            log.debug("New RequestSerializer created.");
        }

        @Override
        protected void serializeRequest(ServiceRequest request, SOAPElement soapRequest, SOAPEnvelope envelope)
                throws SOAPException {
            // Write everything except possible attachment reference to request body
            // SOAPElement
            writeBodyContents(request, soapRequest);
            if (this.requestBody != null && !this.requestBody.isEmpty()) {
                if (!convertPost) {
                    // send the entire HTTP POST as an attachment
                    handleAttachment(request, soapRequest, envelope, this.requestBody);
                }
                // converted HTTP POST is sent inside SOAP body
                // (already handled in serializer.writeBodyContents)
            }
        }

        /**
         * Copies resourceId, GET params (if any) coming from the GET URL, and converted
         * JSON->XML (if any) to the SOAP request body
         *
         * @param request
         * @param soapRequest
         * @throws SOAPException
         */
        protected void writeBodyContents(ServiceRequest request, SOAPElement soapRequest) throws SOAPException {
            if (this.resourceId != null && !this.resourceId.isEmpty()) {
                log.debug("Add resourceId : \"{}\".", this.resourceId);
                soapRequest.addChildElement("resourceId").addTextNode(this.resourceId);
            }
            // requestData contains request parameters and possible converted JSON
            // body, as initialized in ConsumerGateway.processRequest
            SOAPElement containerElement = (SOAPElement) request.getRequestData();
//            Document targetDocument = soapRequest.getOwnerDocument();
//
//            SOAPElement importedElement = (SOAPElement) targetDocument.importNode(containerElement, true);
            // SOAPHelper.moveChildren(importedElement, soapRequest, true);


            NodeList children = containerElement.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = (Node) children.item(i);
                child.setParentElement(soapRequest);
                if ((child.getNamespaceURI() == null || child.getNamespaceURI().isEmpty())) {
                    child = updateNamespaceAndPrefix(child, soapRequest.getNamespaceURI(), soapRequest.getPrefix());
                    updateNamespaceAndPrefix(child.getChildNodes(), soapRequest.getNamespaceURI(), soapRequest.getPrefix());
                }
            }
        }

        /**
         * Updates the namespace URI and prefix of all the nodes in the list, if
         * node does not have namespace URI yet. The list is updated recursively, so
         * also the children of children (and so on) will be updated.
         *
         * @param list      list of nodes to be updated
         * @param namespace target namespace
         * @param prefix    target prefix
         */
        public static void updateNamespaceAndPrefix(NodeList list, String namespace, String prefix) {
            for (int i = 0; i < list.getLength(); i++) {
                Node node = (Node) list.item(i);
                if (node.getNamespaceURI() == null || node.getNamespaceURI().isEmpty()) {
                    node = updateNamespaceAndPrefix(node, namespace, prefix);
                }
                updateNamespaceAndPrefix(node.getChildNodes(), namespace, prefix);
            }
        }

        /**
         * Updates the namespace URI and prefix of the given node with the given
         * values. If prefix is null or empty, only namespace URI is updated.
         *
         * @param node      Node to be updated
         * @param namespace target namespace
         * @param prefix    target prefix
         * @return updated Node
         */
        public static Node updateNamespaceAndPrefix(Node node, String namespace, String prefix) {
            if (node.getNodeType() == ELEMENT_NODE) {
                if (prefix != null && !prefix.isEmpty()) {
                    node = (Node) node.getOwnerDocument().renameNode(node, namespace, prefix + ":" + node.getLocalName());
                } else if (namespace != null && !namespace.isEmpty()) {
                    node = (Node) node.getOwnerDocument().renameNode(node, namespace, node.getLocalName());
                }
            }
            return node;
        }

        protected void handleAttachment(ServiceRequest request, SOAPElement soapRequest, SOAPEnvelope envelope,
                                        String attachmentData) throws SOAPException {
            log.debug("Request body was found from the request. Add request body as SOAP attachment."
                    + " Content type is \"{}\".", this.contentType);
            SOAPElement data = soapRequest.addChildElement(envelope.createName(Constants.PARAM_REQUEST_BODY));
            data.addAttribute(envelope.createName("href"), Constants.PARAM_REQUEST_BODY);
            AttachmentPart attachPart = request.getSoapMessage().createAttachmentPart(attachmentData, this.contentType);
            attachPart.setContentId(Constants.PARAM_REQUEST_BODY);
            request.getSoapMessage().addAttachmentPart(attachPart);

        }
    }

    /**
     * Convert a JSON string to XML. JSON is wrapped inside an extra root element
     * JSON_CONVERSION_WRAPPER_ELEMENT, so the resulting XML is inside a root
     * element with same name
     *
     * @param json
     * @return
     * @throws SOAPException
     */
    private static String convertJsonToXml(String json) throws SOAPException {
        log.debug("converting json: {}", json);
        // create a json wrapper root property
        final StringBuilder wrapped = new StringBuilder();
        wrapped.append("{ \"").append(JSON_CONVERSION_WRAPPER_ELEMENT).append("\": ").append(json).append("}");
        final String converted = new JSONToXMLConverter().convert(wrapped.toString());
        if (converted == null || converted.isEmpty()) {
            throw new SOAPException("could not convert json to xml");
        }
        return converted;
    }

    private class EncryptingRequestSerializer extends RequestSerializer {

        private final Encrypter asymmetricEncrypter;
        private final int keyLength;

        EncryptingRequestSerializer(String resourceId, String requestBody, String contentType,
                                    Encrypter asymmetricEncrypter, int keyLength, boolean convertPost) {
            super(resourceId, requestBody, contentType, convertPost);
            this.asymmetricEncrypter = asymmetricEncrypter;
            this.keyLength = keyLength;
            log.debug("New EncryptingRequestSerializer created.");
        }

        @Override
        protected void serializeRequest(ServiceRequest request, SOAPElement soapRequest, SOAPEnvelope envelope)
                throws SOAPException {
            SOAPElement payload = SOAPHelper.xmlStrToSOAPElement("<" + Constants.PARAM_ENCRYPTION_WRAPPER + "/>");
            try {
                // Create new symmetric encrypter using of defined key length
                Encrypter symmetricEncrypter = RESTGatewayUtil.createSymmetricEncrypter(this.keyLength);
                // Write everything except possible attachment reference to request body
                // SOAPElement
                writeBodyContents(request, payload);
                // Process request body
                if (this.requestBody != null && !this.requestBody.isEmpty()) {
                    if (!convertPost) {
                        // send the entire HTTP POST as an attachment
                        handleAttachment(request, payload, envelope, symmetricEncrypter.encrypt(this.requestBody));
                    }
                    // converted HTTP POST is sent inside SOAP body
                    // (already handled in serializer.writeBodyContents)
                }
                // Encrypt message with symmetric AES encryption
                String encryptedData = symmetricEncrypter.encrypt(SOAPHelper.toString(payload));
                // Build message body that includes encrypted data,
                // encrypted session key and IV
                RESTGatewayUtil.buildEncryptedBody(symmetricEncrypter, asymmetricEncrypter, soapRequest, encryptedData);
            } catch (NoSuchAlgorithmException ex) {
                log.error(ex.getMessage(), ex);
                throw new SOAPException("Encrypting SOAP request failed.", ex);
            }
        }
    }

    /**
     * Deserializes SOAP responses to String.
     */
    private class ResponseDeserializer extends AbstractResponseDeserializer<Map, String> {

        protected boolean omitNamespace;

        ResponseDeserializer(boolean omitNamespace) {
            this.omitNamespace = omitNamespace;
        }

        @Override
        protected Map deserializeRequestData(Node requestNode) throws SOAPException {
            return null;
        }

        @Override
        protected String deserializeResponseData(Node responseNode, SOAPMessage message) throws SOAPException {
            // Remove namespace if it's required
            handleNamespace(responseNode);

            // If message has attachments, return the first attachment
            if (message.countAttachments() > 0) {
                log.debug("SOAP attachment detected. Use attachment as response data.");
                return SOAPHelper.toString((AttachmentPart) message.getAttachments().next());
            }
            // Convert response to string
            return SOAPHelper.toString(responseNode);
        }

        protected void handleNamespace(Node responseNode) {
            try {
                Document document = responseNode.getOwnerDocument(); // Get the document of the responseNode
                if (document != null) {
                    Node importedNode = (Node) document.importNode(responseNode, true); // Import the node into the correct document
                    // Perform namespace removal on the imported node
                    if (importedNode instanceof Element) {
                        ((Element) importedNode).removeAttribute("xmlns");
                        ((Element) importedNode).setPrefix(null);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to remove namespace from node.", e);
            }
        }
    }

    /**
     * Deserializes SOAP responses to String.
     */
    private class EncryptingResponseDeserializer extends ResponseDeserializer {

        private final Decrypter asymmetricDecrypter;

        EncryptingResponseDeserializer(boolean omitNamespace, Decrypter asymmetricDecrypter) {
            super(omitNamespace);
            this.asymmetricDecrypter = asymmetricDecrypter;
            log.debug("New EncryptingResponseDeserializer created.");
        }

        @Override
        protected Map deserializeRequestData(Node requestNode) throws SOAPException {
            return null;
        }

        @Override
        protected String deserializeResponseData(Node responseNode, SOAPMessage message) throws SOAPException {
            // Put all the response nodes to map so that we can easily access them
            Map<String, String> nodes = SOAPHelper.nodesToMap(responseNode.getChildNodes());

            /**
             * N.B.! If signature is present (B: encrypt then sign) and we want to verify it
             * before going ahead with processing, this is the place to do it. The signed
             * part of the message is accessed: nodes.get(Constants.PARAM_ENCRYPTED)
             */
            // Decrypt session key using the private key
            Decrypter symmetricDecrypter = RESTGatewayUtil.getSymmetricDecrypter(this.asymmetricDecrypter,
                    nodes.get(Constants.PARAM_KEY), nodes.get(Constants.PARAM_IV));

            // If message has attachments, return the first attachment
            if (message.countAttachments() > 0) {
                log.debug("SOAP attachment detected. Use attachment as response data.");
                // Return decrypted data
                return symmetricDecrypter
                        .decrypt(SOAPHelper.toString((AttachmentPart) message.getAttachments().next()));
            }
            /**
             * N.B.! If signature is present (A: sign then encrypt) and we want to verify it
             * before going ahead with processing, this is the place to do it. The signed
             * part of the message is accessed:
             * symmetricDecrypter.decrypt(nodes.get(Constants.PARAM_ENCRYPTED)) UTF-8 String
             * wrapper must left out from signature verification.
             */

            // Decrypt the response. UTF-8 characters are not showing correctly
            // if we don't wrap the decrypted data into a new string and
            // explicitly tell that it's UTF-8 even if encrypter and
            // decrypter handle strings as UTF-8.
            String decryptedResponse = new String(
                    symmetricDecrypter.decrypt(nodes.get(Constants.PARAM_ENCRYPTED)).getBytes(StandardCharsets.UTF_8));
            // Convert encrypted response to SOAP
            SOAPElement encryptionWrapper = SOAPHelper.xmlStrToSOAPElement(decryptedResponse);
            // Remove all the children under response node
            SOAPHelper.removeAllChildren(responseNode);
            // Remove the extra <encryptionWrapper> element between response node
            // and the actual response. After the modification all the response
            // elements are directly under response.
            SOAPHelper.moveChildren(encryptionWrapper, (SOAPElement) responseNode, !this.omitNamespace);
            // Clone response node because removing namespace from the original
            // node causes null pointer exception in AbstractResponseDeserializer
            // when wrappers are not used. Cloning the original node, removing
            // namespace from the clone and returning the clone prevents the
            // problem to occur.
            Node modifiedResponseNode = (Node) responseNode.cloneNode(true);
            // Remove namespace if it's required
            handleNamespace(modifiedResponseNode);
            // Return the response
            return SOAPHelper.toString(modifiedResponseNode);
        }
    }

    protected static class GatewayProperties {
        private Properties endpointProps;
        private Properties consumerGatewayProps;

        public GatewayProperties(Properties endpointProps, Properties consumerGatewayProps) {
            this.endpointProps = endpointProps;
            this.consumerGatewayProps = consumerGatewayProps;
        }

        public Properties getEndpointProps() {
            return endpointProps;
        }

        public Properties getConsumerGatewayProps() {
            return consumerGatewayProps;
        }

    }

}
