The first step is to obtain the server's public certificate. That can be done in a variety of ways, such as contacting the server admin and asking for it, using openssl to download it, or, since it's an HTTP server, connecting to it with any browser, viewing the page's security info, and saving a copy of the certificate. 

## Import in native deployment

Now that you have the certificate saved in a file, you need to add it to your JVM's trust store. At ```$JAVA_HOME/jre/lib/security/``` for JDKs or ```$JAVA_HOME/lib/security``` for JREs, there's a file named cacerts, which comes with Java and contains the public certificates of the well-known Certifying Authorities. To import the new cert, run keytool as a user who has permission to write to cacerts:
```
keytool -importcert -alias <some meaningful name> -file <the cert file> -cacerts 
```

## Import in docker container

With the certificate file saved on host, the first step is copying it to the ```/tmp``` directory of the docker container. Therefore, from host run:

```
docker cp <the cert file> <container id>:/tmp
```

Now that the certificate is in the container, we can use the keytool command to import it into the JVM's trust store. 
At ```/opt/java/openjdk/lib/security``` there should be a file named cacerts, which comes with Java and contains the public certificates of the well-known Certifying Authorities.
In order to import the new cert, we need to run keytool in docker container as a user who has permission to write to cacerts.

```
keytool -importcert -alias <some meaningful name> -file /tmp/<cert name> -cacerts
```

**N.B.** The certificate is not persistent over a container's lifecycle and therefore, it must be imported again every time when spinning up a new container. 

