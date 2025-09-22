## TLS connection for REST-adapter-service

When the REST Adapter Service communicates as an HTTP client over HTTPS, the server’s TLS certificate must be trusted. In the consumer gateway role, this means importing the Security Server’s internal TLS certificate into the REST Adapter Service’s Java trust store. In the provider gateway role, it means importing the REST service’s TLS certificate.

### Adapter acting as a client for TLS

You need to get the server certificate (e.g. from an Security Server administrator) and import it into a java truststore. You can use this command:

```shell    
keytool -importcert -noprompt -alias <meaningful-alias> -file <certificate-name>.cer -keystore truststore.p12 -storetype PKCS12 -storepass '<password>'
```

Then you need to mount the truststore into the Docker container and configure Java to use that truststore along with the correct password.
To do so, please add this section to your Docker run command:

```shell
-v <path to truststore on host>:<path to truststore on container>:ro \
-e JAVA_TOOL_OPTIONS="-Djavax.net.ssl.trustStore=<path to truststore on container> -Djavax.net.ssl.trustStorePassword=<password>"
```

### Adapter acting as a client for mTLS 
For mutual TLS (mTLS) authentication, both the client and server must present valid certificates to each other. Therefore, the adapters certificate must be added to the Security Server and a keystore needs to be provided on the adapter. 
Having created a TLS .crt and .key, you can create the keystore or add a key to it using the following command:

```shell
openssl pkcs12 -export -in <certificate-name>.crt -inkey <key-name>.key -name <meaningful-alias> -out <keystore-name>.p12 -passout pass:<password>
```

Then, you need to mount the directory containing the keystore into the container and configure Java to use that keystore along with the correct password, additionally to the provided truststore options.
You can do so by adding the following lines to your `docker run` command:

```shell
 -v <path to keystore on host>:<path to keystore on container>:ro \
 -e JAVA_TOOL_OPTIONS="-Djavax.net.ssl.keyStore=<path to keystore on container> -Djavax.net.ssl.keyStorePassword=<password> -Djavax.net.ssl.trustStore=<path to truststore on container> -Djavax.net.ssl.trustStorePassword=<password>" 
```

N.B.: There is no system property to pick a specific key alias from the keystore, as you can see [here](https://docs.oracle.com/en/java/javase/21/security/java-secure-socket-extension-jsse-reference-guide.html#GUID-0ACD9274-607C-49BE-AED9-BEE2B4F2BEF2), instead if your keystore contains multiple keys, a suitable key is dynamically chosen based on the server's requested certificate types. The default implementation for this is described [here](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/javax/net/ssl/X509KeyManager.html)

### Adapter acting as a server for TLS
When you want to access the adapter itself over HTTPS, e.g. running it as provider-side adapter, accessing it by Security Server, you need to provide a keystore containing the TLS certificate and private key. 
You can mount the volume to a keystore containing a suitable key and provide the necessary environment variables into your Docker container to configure using the keystore. 
Please follow the instructions at [Adapter acting as a client for mTLS](#adapter-acting-as-a-client-for-mtls-) to create a keystore if you do not have one already:

```shell
    -v <path to keystore on host>:<path to keystore on container>:ro \
    -e SERVER_SSL_KEY_STORE=file:<path to keystore on container>.p12 \
    -e SERVER_SSL_KEY_STORE_PASSWORD=<password> \
    -e SERVER_SSL_KEY_STORE_TYPE=PKCS12 \
    -e SERVER_SSL_KEY_ALIAS=<key alias>
```