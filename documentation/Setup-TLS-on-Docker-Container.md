## Secure TLS connection for REST-adapter-service

When the REST Adapter Service communicates as an HTTP client over HTTPS, the server’s TLS certificate must be trusted. In the consumer gateway role, this means importing the Security Server’s internal TLS certificate into the REST Adapter Service’s Java trust store. In the provider gateway role, it means importing the REST service’s TLS certificate.

### Providing Server Certificates for Adapter for TLS

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

## Providing Adapter Certificates for mTLS 
For mutual TLS (mTLS) authentication, both the client and server must present valid certificates to each other. Therefore, the adapters certificate must be added to the Security Server and a keystore needs to be provided on the adapter. 
Having created a TLS .crt and .key, you can create the keystore using the following command:

```shell
openssl pkcs12 -export -in <certificate-name>.crt -inkey <key-name>.key -name <meaningful-alias> -out <keystore-name>.p12 -passout pass:<password>
```

Then, you need to mount the directory containing the keystore into the container and configure Java to use that keystore along with the correct password, additionally to the provided truststore options.
You can do so by adding the following lines to your `docker run` command:

```shell
 -v <path to keystore on host>:<path to keystore on container>:ro \
 -e JAVA_TOOL_OPTIONS="-Djavax.net.ssl.keyStore=<path to keystore on container> -Djavax.net.ssl.keyStorePassword=<password> -Djavax.net.ssl.trustStore=<path to truststore on container> -Djavax.net.ssl.trustStorePassword=<password>" 
```