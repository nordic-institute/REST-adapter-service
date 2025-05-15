The first step is to obtain the server's public certificate. That can be done in a variety of ways, such as contacting the server admin and asking for it, using openssl to download it, or, since it's an HTTP server, connecting to it with any browser, viewing the page's security info, and saving a copy of the certificate. 

Now that you have the certificate saved in a file, you need to add it to your JVM's trust store. At ```$JAVA_HOME/jre/lib/security/``` for JDKs or ```$JAVA_HOME/lib/security``` for JREs, there's a file named cacerts, which comes with Java and contains the public certificates of the well-known Certifying Authorities. To import the new cert, run keytool as a user who has permission to write to cacerts:

```
keytool -importcert -alias <some meaningful name> -file <the cert file> -keystore <path to cacerts file> 
```
