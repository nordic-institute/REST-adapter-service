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
package org.niis.xroad.restadapterservice.util;

import org.niis.xrd4j.common.member.ConsumerMember;
import org.niis.xrd4j.common.member.ProducerMember;
import org.niis.xrd4j.common.security.Encrypter;
import org.niis.xrd4j.common.util.ConfigurationHelper;
import org.niis.xrd4j.common.util.MessageHelper;
import org.niis.xroad.restadapterservice.endpoint.ConsumerEndpoint;

import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class provides utility methods for Consumer Gateway implementation.
 *
 * @author Petteri Kivimäki
 */
@Slf4j
public final class ConsumerGatewayUtil {

    /**
     * This a utility class providing only static methods which is why it should
     * not be initiated.
     */
    private ConsumerGatewayUtil() {

    }

    /**
     * Goes through the given properties and extracts all the defined consumer
     * endpoints. Returns a map containing service id - consumer endpoint
     * key-value pairs.
     *
     * @param endpoints         consumer properties
     * @param gatewayProperties REST Consumer Gateway general properties
     * @return map containing service id - consumer endpoint key-value pairs
     */
    public static Map<String, ConsumerEndpoint> extractConsumers(Properties endpoints, Properties gatewayProperties) {
        Map<String, ConsumerEndpoint> results = new TreeMap<>();
        log.info("Start extracting consumer endpoints from properties.");
        if (endpoints == null || endpoints.isEmpty()) {
            log.warn("No endpoints were founds. The list was null or empty.");
            return results;
        }

        int i = 0;
        String key = Integer.toString(i);

        // Loop through all the endpoints
        while (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_ID)) {

            String clientId = gatewayProperties.getProperty(Constants.CONSUMER_PROPS_ID_CLIENT);
            String serviceId = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_ID);
            String path = endpoints.getProperty(key + "." + Constants.CONSUMER_PROPS_PATH);

            if (RESTGatewayUtil.isNullOrEmpty(serviceId) || RESTGatewayUtil.isNullOrEmpty(path)) {
                log.warn("ID or path is null or empty. Consumer endpoint skipped.");
                key = Integer.toString(++i);
                continue;
            }
            // Path must end with "/"
            if (!path.endsWith("/")) {
                path += "/";
            }

            log.info("New consumer endpoint found. ID : \"{}\", path : \"{}\".", serviceId, path);

            // Create a new ConsumerEndpoint and set default values
            ConsumerEndpoint endpoint = new ConsumerEndpoint(serviceId, clientId, path);
            setDefaultValues(endpoint, gatewayProperties);

            // Client id, HTTP verb, modify URL's
            extractEndpoints(key, endpoints, endpoint);
            // Wrapper processing, ServiceRequest namespace,
            // ServiceResponse namespace, ServiceResponse namespace prefix
            RESTGatewayUtil.extractEndpoints(key, endpoints, endpoint);

            // Create Consumer and ProducerMember objects
            if (ConsumerGatewayUtil.setConsumerAndProducer(endpoint)) {
                // Set namespaces
                endpoint.getProducer().setNamespaceUrl(endpoint.getNamespaceSerialize());
                endpoint.getProducer().setNamespacePrefix(endpoint.getPrefix());
                results.put(endpoint.getHttpVerb() + " " + path, endpoint);
            }
            // Increase counter by one and update key
            key = Integer.toString(++i);
        }

        log.info("{} consumer endpoints extracted from properties.", results.size());
        return ((TreeMap) results).descendingMap();
    }

    /**
     * Extracts properties common for consumer endpoints from the given
     * properties.
     *
     * @param key       property key
     * @param endpoints list of configured endpoints read from properties
     * @param endpoint  the endpoint object that's being initialized
     */
    public static void extractEndpoints(String key, Properties endpoints, ConsumerEndpoint endpoint) {
        // Set more specific endpoint properties
        // Client id
        if (endpoints.containsKey(key + "." + Constants.CONSUMER_PROPS_ID_CLIENT)) {
            String value = endpoints.getProperty(key + "." + Constants.CONSUMER_PROPS_ID_CLIENT);
            endpoint.setClientId(value);
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.CONSUMER_PROPS_ID_CLIENT, value);
        }
        // HTTP verb
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_VERB)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_VERB);
            if (value != null) {
                value = value.toUpperCase();
            }
            endpoint.setHttpVerb(value);
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_VERB, value);
        }
        // Modify URLs
        if (endpoints.containsKey(key + "." + Constants.CONSUMER_PROPS_MOD_URL)) {
            String value = endpoints.getProperty(key + "." + Constants.CONSUMER_PROPS_MOD_URL);
            endpoint.setModifyUrl(MessageHelper.strToBool(value));
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.CONSUMER_PROPS_MOD_URL, value);
        }
        // Convert POST JSON messages
        if (endpoints.containsKey(key + "." + Constants.CONSUMER_PROPS_CONVERT_POST)) {
            String value = endpoints.getProperty(key + "." + Constants.CONSUMER_PROPS_CONVERT_POST);
            endpoint.setConvertPost(MessageHelper.strToBool(value));
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.CONSUMER_PROPS_CONVERT_POST, value);
        }

    }

    /**
     * Constructs and initializes a ConsumerMember object and a ProducerMember
     * object related to the given ConsumerEndpoint. The ConsumerMember is
     * constructed according to the value of the clientId variable. The
     * ProducerMember is constructed according to the value of the serviceId
     * variable.
     *
     * @param endpoint ConsumerEndpoint object
     * @return true if and only if creating ConsumerMember object and
     * ProducerMember objects succeeded; otherwise false
     */
    protected static boolean setConsumerAndProducer(ConsumerEndpoint endpoint) {
        // Create ProducerMember object
        if (!ConsumerGatewayUtil.setProducerMember(endpoint)) {
            log.warn("Creating producer member failed. Consumer endpoint skipped.");
            return false;
        } // Create ConsumerMember object
        if (!ConsumerGatewayUtil.setConsumerMember(endpoint)) {
            log.warn("Creating consumer member failed. Consumer endpoint skipped.");
            return false;
        }
        return true;
    }

    /**
     * Sets default values to the given endpoint.
     *
     * @param endpoint          ConsumerEndpoint to be modified
     * @param gatewayProperties REST Consumer Gateway general properties
     */
    private static void setDefaultValues(ConsumerEndpoint endpoint, Properties gatewayProperties) {
        // Initialize endpoint properties to those defined in gateway properties
        endpoint.setNamespaceDeserialize(gatewayProperties.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE));
        endpoint.setNamespaceSerialize(gatewayProperties.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE));
        endpoint.setPrefix(gatewayProperties.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE));
        if (gatewayProperties.containsKey(Constants.ENDPOINT_PROPS_WRAPPERS)) {
            endpoint.setProcessingWrappers(MessageHelper.strToBool(gatewayProperties.getProperty(Constants.ENDPOINT_PROPS_WRAPPERS)));
        }
        if (gatewayProperties.containsKey(Constants.CONSUMER_PROPS_CONVERT_POST)) {
            endpoint.setConvertPost(MessageHelper.strToBool(gatewayProperties.getProperty(Constants.CONSUMER_PROPS_CONVERT_POST)));
        }

        // Set default HTTP verb
        endpoint.setHttpVerb("GET");
    }

    /**
     * Goes through the given endpoint and tries to find an endpoint which id
     * matches the given service id. If no match is found, then tries to find a
     * partial match.
     *
     * @param serviceId service id to be looked for
     * @param endpoints list of endpoints
     * @return endpoint matching the given service id or null
     */
    public static ConsumerEndpoint findMatch(String serviceId, Map<String, ConsumerEndpoint> endpoints) {
        if (endpoints.containsKey(serviceId)) {
            log.debug("Found match by service id : \"{}\".", serviceId);
            return endpoints.get(serviceId);
        }
        for (Map.Entry<String, ConsumerEndpoint> entry : endpoints.entrySet()) {
            String key = entry.getKey();
            String keyMod = key.replaceAll("\\{" + Constants.PARAM_RESOURCE_ID + "\\}", "([\\\\w\\\\-]+?)");
            log.trace("Modified key used for comparison : \"{}\".", keyMod);
            if (serviceId.matches(keyMod)) {
                log.debug("Found partial match by service id. Request value : \"{}\", matching value : \"{}\".", serviceId, key);
                ConsumerEndpoint endpoint = entry.getValue();
                // Parse resource id and set it to endpoint
                parseResourceId(key, endpoint, serviceId);
                return endpoint;
            }
        }
        log.debug("No match by service id was found. Service id : \"{}\".", serviceId);
        return null;
    }

    /**
     * Parses the resource id from the given service id and sets endpoint's
     * resource id variable. E.g. key is "GET /myhost.com/service/{resourceId}"
     * and service id is "GET /myhost.com/service/123". Based on these two
     * strings we can parse the resource id "123" from the service id.
     *
     * @param key       service identifier as a String from properties
     * @param endpoint  Endpoint object representing the identifier
     * @param serviceId service identifier as a String that has been called
     */
    private static void parseResourceId(String key, ConsumerEndpoint endpoint, String serviceId) {
        // Get the index of '{' character
        int index = key.indexOf('{');
        if (index != -1) {
            // Get the resource id - starts from the first "{"
            String resourceId = serviceId.substring(index);
            // If the last character is "/", remove it
            if (resourceId.endsWith("/")) {
                resourceId = resourceId.substring(0, resourceId.length() - 1);
            }
            endpoint.setResourceId(resourceId);
            log.trace("Set resource id : \"{}\"", resourceId);
        }
    }

    /**
     * Rewrites all the URLs in the responseStr that are matching the
     * resourcePath to point the Consumer Gateway servlet.
     *
     * @param servletUrl     URL of Consumer Gateway serlvet
     * @param pathToResource path that's rewritten to point the Consumer Gateway
     * @param responseStr    response to be modified
     * @return modified response
     */
    public static String rewriteUrl(String servletUrl, String pathToResource, String responseStr) {
        log.debug("Rewrite URLs in the response to point Consumer Gateway.");
        log.debug("Consumer Gateway URL : \"{}\".", servletUrl);
        try {
            // Remove "/{resourceId}" from resource path, and omit
            // first and last slash ('/') character
            String resourcePath = pathToResource.substring(1, pathToResource.length() - 1).replaceAll("/\\{" + Constants.PARAM_RESOURCE_ID + "\\}", "");
            log.debug("Resourse URL that's replaced with Consumer Gateway URL : \"http(s)://{}\".", resourcePath);
            log.debug("New resource URL : \"{}{}\".", servletUrl, resourcePath);
            // Modify the response
            return responseStr.replaceAll("http(s|):\\/\\/" + resourcePath, servletUrl + resourcePath);
        } catch (Exception ex) {
            log.error("Rewriting the URLs failed!");
            log.error(ex.getMessage(), ex);
            return responseStr;
        }
    }

    /**
     * Constructs and initializes a ConsumerMember object related to the given
     * ConsumerEndpoint. The object is constructed according to the value of the
     * clientId variable.
     *
     * @param endpoint ConsumerEndpoint object
     * @return true if and only if creating ConsumerMember object succeeded;
     * otherwise false
     */
    protected static boolean setConsumerMember(ConsumerEndpoint endpoint) {
        ConsumerMember consumer = ConfigurationHelper.parseConsumerMember(endpoint.getClientId());
        if (consumer == null) {
            log.warn("ConsumerMember not found.");
            return false;
        }
        endpoint.setConsumer(consumer);
        log.debug("ConsumerMember id : \"{}\".", consumer.toString());
        return true;
    }

    /**
     * Constructs and initializes a ProducerMember object related to the given
     * ConsumerEndpoint. The object is constructed according to the value of the
     * serviceId variable.
     *
     * @param endpoint ConsumerEndpoint object
     * @return true if and only if creating ProducerMember object succeeded;
     * otherwise false
     */
    protected static boolean setProducerMember(ConsumerEndpoint endpoint) {
        ProducerMember producer = ConfigurationHelper.parseProducerMember(endpoint.getServiceId());
        if (producer == null) {
            log.warn("ProducerMember not found.");
            return false;
        }
        endpoint.setProducer(producer);
        log.debug("ProducerMember id : \"{}\".", producer.toString());
        return true;
    }

    /**
     * Creates a ConsumerEndpoint that points to a service which configuration
     * information is not in the configuration file. Resource path is used as
     * service id.
     *
     * @param props          consumer gateway properties that contain default namespace
     *                       and prefix
     * @param pathToResource resource path that was called, used as service id
     * @return ConsumerEndpoint object
     */
    public static ConsumerEndpoint createUnconfiguredEndpoint(Properties props, String pathToResource) {
        log.debug("Create a consumer endpoint that points to a service defined by resource path.");
        String resourceId = null;
        String resourcePath;
        // Check if a resource id is present in the resource path.
        // Pattern for resource path and resource id.
        String pattern = "/(.+?)/(.+)";
        Pattern regex = Pattern.compile(pattern);
        Matcher m = regex.matcher(pathToResource);
        // If resource id is found, split resource id and resource path
        if (m.find()) {
            resourcePath = m.group(1);
            resourceId = m.group(2).substring(0, m.group(2).length() - 1);
            log.info("Resource id detected. Resource path : \"{}\". Resource id : \"{}\".", resourcePath, resourceId);
        } else {
            // Remove slashes, they're not part of service id
            resourcePath = pathToResource.replaceAll("/", "");
        }
        // Get client id
        String clientId = props.getProperty(Constants.CONSUMER_PROPS_ID_CLIENT);
        // Create new endpoint
        ConsumerEndpoint endpoint = new ConsumerEndpoint(resourcePath, clientId, "");
        // Set resource id
        endpoint.setResourceId(resourceId);
        // Parse consumer and producer from ids
        if (!ConsumerGatewayUtil.setConsumerMember(endpoint) || !ConsumerGatewayUtil.setProducerMember(endpoint)) {
            // Set endpoint to null if parsing failed
            endpoint = null;
        } else {
            // Get default namespace and prefix from properties
            String ns = props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE);
            String prefix = props.getProperty(Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE);
            // Set namespace and prefix
            endpoint.getProducer().setNamespaceUrl(ns);
            endpoint.getProducer().setNamespacePrefix(prefix);
        }
        return endpoint;
    }

    /**
     * Removes response tag and its namespace prefixes from the given response
     * message. All the response tag's namespace prefixes are removed from the
     * children. Response tag can be simple \<response\> or prefixed with
     * service name \<serviceNameResponse\>.
     *
     * @param message response message
     * @return children of the response tag
     */
    public static String removeResponseTag(String message) {
        String responsePrefix = "";
        // Regex for reponse tag's namespace prefix
        String regex = ".*<(\\w+:)*(\\w+R|r)esponse.*?>.*";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            if (matcher.group(1) != null) {
                responsePrefix = matcher.group(1);
                log.debug("Response tag's prefix is \"{}\".", responsePrefix);
            } else {
                log.debug("No namespace prefix was found for response tag.");
            }
        }
        // If content type is XML the message doesn't have to
        // be converted, but it should be checked if there
        // are additional <response> tags as a wrapper
        String response = message.replaceAll("<(/)*" + responsePrefix + "(\\w+R|r)esponse.*?>", "");
        // Remove response tag's prefixes, because otherwise
        // the conversion to SOAP element will fail because
        // of them
        if (!responsePrefix.isEmpty()) {
            response = response.replaceAll("(</{0,1})" + responsePrefix, "$1");
        }
        return response;
    }

    /**
     * Checks and validates the properties related to encryption. The method
     * checks that all the necessary private and public keys exist and are
     * accessible. If everything is OK, true is returned. If there's a problem
     * with one or more keys and/or private key is was not checked, false is
     * returned.
     *
     * @param props                    general properties
     * @param endpoints                list of configured endpoints
     * @param asymmetricEncrypterCache cache variable for asymmetric encrypters.
     *                                 All the asymmetric encrypters that are successfully checked are added to
     *                                 the cache.
     * @return true if everything is OK. False if there's a problem with one or
     * more keys and/or private key is was not checked.
     */
    public static boolean checkEncryptionProperties(Properties props, Map<String, ConsumerEndpoint> endpoints,
                                                    Map<String, Encrypter> asymmetricEncrypterCache) {
        log.info("Check encryption properties.");
        boolean result = true;
        boolean mustCheckPrivateKey = false;
        // Loop through all the endpoints
        for (Map.Entry<String, ConsumerEndpoint> entry : endpoints.entrySet()) {
            ConsumerEndpoint endpoint = entry.getValue();
            // If request is encrypted, encryption is done using the public
            // key of the receiver, so it must be possible to access it.
            if (endpoint.isRequestEncrypted()) {
                String producerId = endpoint.getProducer().toString();
                Encrypter encrypter = RESTGatewayUtil.checkPublicKey(props, producerId);
                if (encrypter == null) {
                    log.error("The endpoint \"{}\" does not support encryption of request messages.", producerId);
                    result = false;
                } else {
                    // Add encrypter to cache
                    asymmetricEncrypterCache.put(endpoint.getProducer().toString(), encrypter);
                    log.trace("Asymmetric encrypter for producer \"{}\" was added to cache.", producerId);
                }
            }
            // If response is encrypted, decryption is done using the private
            // key, so it must be possible to access it. It's enough to
            // check the private key once so it can be done outside of the loop.
            if (endpoint.isResponseEncrypted()) {
                mustCheckPrivateKey = true;
            }
        }
        // If access to private key is required, check it
        if (mustCheckPrivateKey && RESTGatewayUtil.checkPrivateKey(props) == null) {
            log.error("None of the endpoints support deccryption of response messages.");
            result = false;
        }
        log.info("Encryption properties checked.");
        return result && mustCheckPrivateKey;
    }
}
