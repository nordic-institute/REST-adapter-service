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
package fi.vrk.xroad.restadapterservice.util;

/**
 * REST Gateway constants.
 *
 * @author Petteri Kivimäki
 */
public class Constants {

    /**
     * Properties files
     */
    public static final String PROPERTIES_FILE_PROVIDER_GATEWAY = "/provider-gateway.properties";
    public static final String PROPERTIES_FILE_CONSUMER_GATEWAY = "/consumer-gateway.properties";
    public static final String PROPERTIES_FILE_PROVIDERS = "/providers.properties";
    public static final String PROPERTIES_FILE_CONSUMERS = "/consumers.properties";
    /**
     * Endpoint properties - used by both provider and consumer endpoints
     *
     */
    public static final String ENDPOINT_PROPS_ID = "id";
    public static final String ENDPOINT_PROPS_VERB = "verb";
    public static final String ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE = "namespace.deserialize";
    public static final String ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE = "namespace.serialize";
    public static final String ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE = "namespace.prefix.serialize";
    public static final String ENDPOINT_PROPS_WRAPPERS = "wrappers";
    public static final String ENDPOINT_PROPS_REQUEST_ENCRYPTED = "request.encrypted";
    public static final String ENDPOINT_PROPS_RESPONSE_ENCRYPTED = "response.encrypted";
    /**
     * Provider properties
     */
    public static final String PROVIDER_PROPS_URL = "url";
    public static final String PROVIDER_PROPS_CONTENT_TYPE = "contenttype";
    public static final String PROVIDER_PROPS_ACCEPT = "accept";
    public static final String PROVIDER_PROPS_ATTACHMENT = "response.attachment";
    public static final String PROVIDER_PROPS_SEND_XRD_HEADERS = "request.xrdheaders";
    public static final String PROVIDER_PROPS_REQUEST_PARAM_NAME_FILTER_CONDITION = "reqParamNameFilterCondition";
    public static final String PROVIDER_PROPS_REQUEST_PARAM_NAME_FILTER_OPERATION = "reqParamNameFilterOperation";
    public static final String PROVIDER_PROPS_REQUEST_PARAM_VALUE_FILTER_CONDITION = "reqParamValueFilterCondition";
    public static final String PROVIDER_PROPS_REQUEST_PARAM_VALUE_FILTER_OPERATION = "reqParamValueFilterOperation";
    /**
     * Consumer properties
     */
    public static final String CONSUMER_PROPS_PATH = "path";
    public static final String CONSUMER_PROPS_ID_CLIENT = "id.client";
    public static final String CONSUMER_PROPS_SECURITY_SERVER_URL = "ss.url";
    public static final String CONSUMER_PROPS_MOD_URL = "response.modurl";
    public static final String CONSUMER_PROPS_SVC_CALLS_BY_XRD_SVC_ID_ENABLED = "serviceCallsByXRdServiceId.enabled";
    /**
     * Encryption properties
     */
    public static final String ENCRYPTION_PROPS_KEY_LENGTH = "keyLength";
    public static final String ENCRYPTION_PROPS_PUBLIC_KEY_FILE = "publicKeyFile";
    public static final String ENCRYPTION_PROPS_PUBLIC_KEY_FILE_PASSWORD = "publicKeyFilePassword";
    public static final String ENCRYPTION_PROPS_PRIVATE_KEY_FILE = "privateKeyFile";
    public static final String ENCRYPTION_PROPS_PRIVATE_KEY_FILE_PASSWORD = "privateKeyFilePassword";
    public static final String ENCRYPTION_PROPS_PRIVATE_KEY_ALIAS = "privateKeyAlias";
    public static final String ENCRYPTION_PROPS_PRIVATE_KEY_PASSWORD = "privateKeyPassword";
    /**
     * Standard HTTP headers
     */
    public static final String HTTP_HEADER_ACCEPT = "Accept";
    public static final String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    /**
     * X-Road specific custom HTTP headers
     */
    public static final String XRD_HEADER_CLIENT = "X-XRd-Client";
    public static final String XRD_HEADER_SERVICE = "X-XRd-Service";
    public static final String XRD_HEADER_USER_ID = "X-XRd-UserId";
    public static final String XRD_HEADER_MESSAGE_ID = "X-XRd-MessageId";
    public static final String XRD_HEADER_NAMESPACE_SERIALIZE = "X-XRd-NamespaceSerialize";
    public static final String XRD_HEADER_NAMESPACE_PREFIX_SERIALIZE = "X-XRd-NamespacePrefixSerialize";
    /**
     * Other constants.
     */
    public static final String PARAM_REQUEST_BODY = "RESTGatewayRequestBody";
    public static final String PARAM_RESOURCE_ID = "resourceId";
    public static final String PARAM_ENCRYPTION_WRAPPER = "encryptionWrapper";
    public static final String PARAM_ENCRYPTED = "encrypted";
    public static final String PARAM_IV = "iv";
    public static final String PARAM_KEY = "key";
    /**
     * Parameter names
     */
    public static final String PROPERTIES_DIR_PARAM_NAME = "propertiesDirectory";
    /**
     * Properties default directory name
     */
    public static final String PROPERTIES_DIR_NAME = "rest-adapter-service";

    /**
     * HTTP error messages
     */
    public static final String ERROR_404 = "404 Not Found";
    public static final String ERROR_422 = "422 Unprocessable Entity. Missing request data.";
    public static final String ERROR_500 = "500 Internal Server Error";
    // HTTP Headers
    /**
     * Content-Type: text/xml
     */
    public static final String TEXT_XML = "text/xml";
    /**
     * Content-Type: application/xml
     */
    public static final String APPLICATION_XML = "application/xml";
    /**
     * Content-Type: application/json
     */
    public static final String APPLICATION_JSON = "application/json";
    /**
     * Character set UTF-8
     */
    public static final String CHARSET_UTF8 = "charset=utf-8";
    /**
     * Constants for logging.
     */
    public static final String LOG_STRING_FOR_SETTINGS = "\"{}\" setting found. Value : \"{}\".";
    public static final String LOG_STRING_FOR_HEADERS = "{} : {}";
    /**
     * Constructs and initializes a new Constants object. Should never be used.
     */
    private Constants() {
    }
}
