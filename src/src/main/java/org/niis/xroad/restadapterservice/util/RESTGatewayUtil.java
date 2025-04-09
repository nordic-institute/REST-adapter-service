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

import org.niis.xrd4j.common.security.AsymmetricDecrypter;
import org.niis.xrd4j.common.security.AsymmetricEncrypter;
import org.niis.xrd4j.common.security.CryptoHelper;
import org.niis.xrd4j.common.security.Decrypter;
import org.niis.xrd4j.common.security.Encrypter;
import org.niis.xrd4j.common.security.SymmetricDecrypter;
import org.niis.xrd4j.common.security.SymmetricEncrypter;
import org.niis.xrd4j.common.util.MessageHelper;
import org.niis.xroad.restadapterservice.endpoint.AbstractEndpoint;

import lombok.extern.slf4j.Slf4j;

import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Properties;

/**
 * This class provides utility methods for REST Gateway implementation.
 *
 * @author Petteri Kivimäki
 */
@Slf4j
public final class RESTGatewayUtil {

    public static final int DEFAULT_KEY_LENGTH = 128;

    /**
     * Constructs and initializes a new RESTGatewayUtil object. Should never be
     * used.
     */
    private RESTGatewayUtil() {
    }

    /**
     * Checks the given content type and returns true if and only if it begins with
     * "text/xml" or "application/xml". Otherwise returns false.
     *
     * @param contentType content type to be checked
     * @return returns true if and only if the given content type begins with
     *         "text/xml" or "application/xml"; otherwise returns false
     */
    public static boolean isXml(String contentType) {
        return contentType != null
                && (contentType.startsWith(Constants.TEXT_XML) || contentType.startsWith(Constants.APPLICATION_XML));
    }

    /**
     * Checks the given content type and returns true if and only if it begins with
     * "text/xml", "application/xml" or "application/json". Otherwise returns false.
     *
     * @param contentType content type to be checked
     * @return returns true if and only if the given content type begins with
     *         "text/xml", "application/xml" or "application/json; otherwise returns
     *         false
     */
    public static boolean isValidContentType(String contentType) {
        return contentType != null
                && (contentType.startsWith(Constants.TEXT_XML) || contentType.startsWith(Constants.APPLICATION_XML)
                        || contentType.startsWith(Constants.APPLICATION_JSON));
    }

    /**
     * Extracts properties common for both consumer and provider endpoints from the
     * given properties.
     *
     * @param key       property key
     * @param endpoints list of configured endpoints read from properties
     * @param endpoint  the endpoint object that's being initialized
     */
    public static void extractEndpoints(String key, Properties endpoints, AbstractEndpoint endpoint) {
        // Wrapper processing
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_WRAPPERS)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_WRAPPERS);
            endpoint.setProcessingWrappers(MessageHelper.strToBool(value));
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_WRAPPERS, value);
        }
        // ServiceRequest namespace
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE);
            endpoint.setNamespaceDeserialize(value);
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_DESERIALIZE, value);
        }
        // ServiceResponse namespace
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE);
            endpoint.setNamespaceSerialize(value);
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_SERIALIZE, value);
        }
        // ServiceResponse namespace prefix
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE)) {
            String value = endpoints
                    .getProperty(key + "." + Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE);
            endpoint.setPrefix(value);
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_SERVICE_NAMESPACE_PREFIX_SERIALIZE,
                    value);
        }
        // Is request encrypted
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_REQUEST_ENCRYPTED)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_REQUEST_ENCRYPTED);
            endpoint.setRequestEncrypted(MessageHelper.strToBool(value));
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_REQUEST_ENCRYPTED, value);
        }
        // Is response encrypted
        if (endpoints.containsKey(key + "." + Constants.ENDPOINT_PROPS_RESPONSE_ENCRYPTED)) {
            String value = endpoints.getProperty(key + "." + Constants.ENDPOINT_PROPS_RESPONSE_ENCRYPTED);
            endpoint.setResponseEncrypted(MessageHelper.strToBool(value));
            log.info(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENDPOINT_PROPS_RESPONSE_ENCRYPTED, value);
        }
    }

    /**
     * Checks the given String for null and empty. Returns true if and only if the
     * string is null or empty.
     *
     * @param value String to be checked
     * @return rue if and only if the string is null or empty; otherwise false
     */
    public static boolean isNullOrEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Checks that accessing the private key is possible using the configuration.
     * The method tries to create a new Decrypter object which is returned if
     * everything is OK. If creating the object fails, null is returned.
     *
     * @param props general properties
     * @return new Decrypter object on success; otherwise false
     */
    public static Decrypter checkPrivateKey(Properties props) {
        String privateKeyFile = props.getProperty(Constants.ENCRYPTION_PROPS_PRIVATE_KEY_FILE);
        String privateKeyFilePassword = props.getProperty(Constants.ENCRYPTION_PROPS_PRIVATE_KEY_FILE_PASSWORD);
        String privateKeyAlias = props.getProperty(Constants.ENCRYPTION_PROPS_PRIVATE_KEY_ALIAS);
        String privateKeyPassword = props.getProperty(Constants.ENCRYPTION_PROPS_PRIVATE_KEY_PASSWORD);
        return getDecrypter(privateKeyFile, privateKeyFilePassword, privateKeyAlias, privateKeyPassword);
    }

    /**
     * Returns a decrypter with the private key.
     *
     * @param privateKeyFile         path to private key file
     * @param privateKeyFilePassword private key file password
     * @param privateKeyAlias        private key alias
     * @param privateKeyPassword     private key password
     * @return public key matching the given service id
     */
    public static Decrypter getDecrypter(String privateKeyFile, String privateKeyFilePassword, String privateKeyAlias,
            String privateKeyPassword) {
        try {
            log.debug("Read private key \"{}\" from keystore.", privateKeyAlias);
            Decrypter decrypter = new AsymmetricDecrypter(privateKeyFile, privateKeyFilePassword, privateKeyAlias,
                    privateKeyPassword);
            log.info("Access to private key \"{}\" checked.", privateKeyAlias);
            return decrypter;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException
                | UnrecoverableEntryException ex) {
            log.error(ex.getMessage(), ex);
            log.debug(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENCRYPTION_PROPS_PRIVATE_KEY_FILE, privateKeyFile);
            log.debug(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENCRYPTION_PROPS_PRIVATE_KEY_FILE_PASSWORD,
                    privateKeyFilePassword.replaceAll(".*", "*"));
            log.debug(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENCRYPTION_PROPS_PRIVATE_KEY_ALIAS, privateKeyAlias);
            log.debug(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENCRYPTION_PROPS_PRIVATE_KEY_PASSWORD,
                    privateKeyPassword.replaceAll(".*", "*"));
            return null;
        } catch (java.lang.NullPointerException ex) {
            log.error(ex.getMessage(), ex);
            log.error("\"{}\" property value is invalid. No private key was found with the given alias value: \"{}\".",
                    Constants.ENCRYPTION_PROPS_PRIVATE_KEY_ALIAS, privateKeyAlias);
            return null;
        }
    }

    /**
     * Checks that accessing the public key matching the given service id is
     * possible using the configuration. The method tries to create a new Encrypter
     * object which is returned if everything is OK. If creating the object fails,
     * null is returned.
     *
     * @param props     general properties
     * @param serviceId unique string that identifies the service which is used as
     *                  public key alias
     * @return new Encrypter object on success; otherwise false
     */
    public static Encrypter checkPublicKey(Properties props, String serviceId) {
        String publicKeyFile = props.getProperty(Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE);
        String publicKeyFilePassword = props.getProperty(Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE_PASSWORD);
        return getEncrypter(publicKeyFile, publicKeyFilePassword, serviceId);
    }

    /**
     * Returns an encrypter using the public key matching the given service id.
     *
     * @param publicKeyFile         path to public key file
     * @param publicKeyFilePassword public key file password
     * @param serviceId             unique ID of the service
     * @return public key matching the given service id
     */
    public static Encrypter getEncrypter(String publicKeyFile, String publicKeyFilePassword, String serviceId) {
        try {
            Encrypter encrypter = new AsymmetricEncrypter(publicKeyFile, publicKeyFilePassword, serviceId);
            log.info("Public key \"{}\" checked and OK.", serviceId);
            return encrypter;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            log.error(ex.getMessage(), ex);
            log.debug(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE, publicKeyFile);
            log.debug(Constants.LOG_STRING_FOR_SETTINGS, Constants.ENCRYPTION_PROPS_PUBLIC_KEY_FILE_PASSWORD,
                    publicKeyFilePassword.replaceAll(".*", "*"));
            log.debug("Service ID is used as public key alias: \"{}\"", serviceId);
            return null;
        } catch (java.lang.NullPointerException ex) {
            log.error("No public key was found for the alias \"{}\".", serviceId);
            log.error(ex.getMessage(), ex);
            return null;
        }
    }

    /**
     * Gets the value of symmetric key length from the properties.
     *
     * @param props general properties
     * @return key length. Returns 128 if property is missing, empty or not a number
     */
    public static int getKeyLength(Properties props) {
        String keyLengthStr = props.getProperty(Constants.ENCRYPTION_PROPS_KEY_LENGTH);
        if (keyLengthStr != null && !keyLengthStr.isEmpty()) {
            try {
                return Integer.parseInt(keyLengthStr);
            } catch (NumberFormatException ex) {
                log.error(ex.getMessage(), ex);
                log.warn("Invalid value for \"{}\" property. Use default 128.", Constants.ENCRYPTION_PROPS_KEY_LENGTH);
                return DEFAULT_KEY_LENGTH;
            }
        }
        return DEFAULT_KEY_LENGTH;
    }

    /**
     * Creates a new symmetric encrypter of the given length.
     *
     * @param keyLength key length in bits
     * @return new symmetric encrypter
     * @throws NoSuchAlgorithmException
     */
    public static Encrypter createSymmetricEncrypter(int keyLength) throws NoSuchAlgorithmException {
        log.debug("Create a new symmetric encrypter.");
        // Create new symmetric AES key that's used for encrypting
        // the message payload
        Key key = CryptoHelper.generateAESKey(keyLength);
        // Get initialization vector (IV) for symmetric encrypter
        byte[] iv = CryptoHelper.generateIV();
        // Create new symmetric encrypter using the key and IV
        return new SymmetricEncrypter(key, iv);
    }

    /**
     * Decrypts symmetric session key using private key of message recipient and
     * asymmetric decrypter. The session key has been encrypted using the public key
     * of the message recipient.
     *
     * @param asymmetricDecrypter asymmetric RSA decrypter
     * @param encryptedKey        encrypted symmetric AES key
     * @param encodedIV           base 64 encoded initialization vector (VI)
     * @return Decrypter created using encrypted AES key and VI
     */
    public static Decrypter getSymmetricDecrypter(Decrypter asymmetricDecrypter, String encryptedKey,
            String encodedIV) {
        log.debug("Decrypt symmetric session key.");
        // Decrypt session key using the private key
        String decryptedSessionKey = asymmetricDecrypter.decrypt(encryptedKey);
        // Convert the decrypted session key from string to key
        Key decodedKey = CryptoHelper.strToKey(decryptedSessionKey);
        // Get IV
        byte[] decodedIv = CryptoHelper.decodeBase64(encodedIV);
        // Symmetric decrypter for the data
        return new SymmetricDecrypter(decodedKey, decodedIv);
    }

    /**
     * Build message body with three elements that include encrypted data, encrypted
     * session key and IV. The three elements are added as child elements of the
     * given SOAP message.
     *
     * @param symmetricEncrypter  symmetric AES encrypter
     * @param asymmetricEncrypter asymmetric RSA encrypter
     * @param msg                 SOAP element container
     * @param encryptedData       encrypted data
     * @throws SOAPException
     */
    public static void buildEncryptedBody(Encrypter symmetricEncrypter, Encrypter asymmetricEncrypter, SOAPElement msg,
            String encryptedData) throws SOAPException {
        log.debug("Build message body with encrypted data, encrypted session key and encoded IV.");
        // Get base 64 encoded version of the key
        String sessionKey = CryptoHelper.encodeBase64(((SymmetricEncrypter) symmetricEncrypter).getKey().getEncoded());
        // Add enrypted data to message
        msg.addChildElement(Constants.PARAM_ENCRYPTED).addTextNode(encryptedData);
        // Encrypt symmetric key with receivers public RSA key and add it to the message
        msg.addChildElement(Constants.PARAM_KEY).addTextNode(asymmetricEncrypter.encrypt(sessionKey));
        // Add base 64 IV to the message
        msg.addChildElement(Constants.PARAM_IV)
                .addTextNode(CryptoHelper.encodeBase64(((SymmetricEncrypter) symmetricEncrypter).getIv()));
    }

    /**
     * Find the properties directory:
     */
    public static String getPropertiesDirectory() {
        final String os = System.getProperty("os.name", "generic").toLowerCase();
        String dir = System.getProperty(Constants.PROPERTIES_DIR_PARAM_NAME);
        if (dir != null) {
            // if defined by the property, it will be an error if the directory does not
            // exist
            return dir;
        }

        Path p = Paths.get(System.getProperty("user.home"), Constants.PROPERTIES_DIR_NAME);
        if (Files.exists(p)) {
            return p.toString();
        }

        if (os.startsWith("linux")) {
            p = Paths.get("/etc", Constants.PROPERTIES_DIR_NAME);
            if (Files.exists(p)) {
                return p.toString();
            }
        }

        return null;
    }
}
