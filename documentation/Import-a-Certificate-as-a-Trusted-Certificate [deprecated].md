## Managing TLS Certificates for the REST Adapter Service

When the REST Adapter Service communicates as an HTTP client over HTTPS, the server’s TLS certificate must be trusted. In the consumer gateway role, this means importing the Security Server’s internal TLS certificate into the REST Adapter Service’s Java trust store. In the provider gateway role, it means importing the REST service’s TLS certificate.

## Importing a TLS Certificate

The first step is to obtain the server's public certificate. That can be done in a variety of ways, such as contacting the server admin and asking for it, using openssl to download it, or, since it's an HTTP server, connecting to it with any browser, viewing the page's security info, and saving a copy of the certificate.

Now that you have the certificate saved in a file, you need to add it to your JVM's trust store. At ```$JAVA_HOME/jre/lib/security/``` for JDKs or ```$JAVA_HOME/lib/security``` for JREs, there's a file named cacerts, which comes with Java and contains the public certificates of the well-known Certifying Authorities. To import the new cert, run keytool as a user who has permission to write to cacerts:

```shell
keytool -importcert -alias <some meaningful name> -file <the cert file> -cacerts
```

The default password is "changeit".