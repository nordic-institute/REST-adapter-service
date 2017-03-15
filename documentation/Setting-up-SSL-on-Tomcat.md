The set up consists of 4 basic steps:

1. Create a keystore file using Java
2. Configure Tomcat to use the keystore
3. Test it
4. How to configure web applications for SSL

#### 1. Create a keystore

Use ```keytool``` command to create a self-signed certificate. The below command creates a ```mykeystore``` keysore in the current working directory.

```
keytool -genkey -alias mycert -keyalg RSA -keystore mykeystore
```

During the keystore creation process, you need to assign a password and fill in the certificate’s detail.

```
> keytool -genkey -alias mycert -keyalg RSA -keystore mykeystore
Enter keystore password:  password
Re-enter new password: password
What is your first and last name?
  [Unknown]:  mydomain.com
What is the name of your organizational unit?
  [Unknown]:  My Unit
What is the name of your organization?
  [Unknown]:  My Organization
What is the name of your City or Locality?
  [Unknown]:  Helsinki
What is the name of your State or Province?
  [Unknown]:  Uusimaa
What is the two-letter country code for this unit?
  [Unknown]:  FI
Is CN=mydomain.com, OU=My Unit, O=My Organization, L=Helsinki, ST=Uusimaa, C=FI correct?
  [no]:  yes
 
Enter key password for
    (RETURN if same as keystore password):  password
Re-enter new password: password
```

This will create a ```mykeystore``` file in the current working directory. 

##### Certificate Details

It's possible to use ```keytool``` command to list the existing certificate's detail.

```
> keytool -list -keystore mykeystore
Enter keystore password:

Keystore type: JKS
Keystore provider: SUN

Your keystore contains 1 entry

mycert, 22.2.2015, PrivateKeyEntry,
Certificate fingerprint (MD5): 52:13:B6:5F:59:28:98:D1:AE:41:E0:96:59:7E:1F:0B 
```

#### 2. Configure Tomcat

Open ```$TOMCAT/conf/server.xml``` file and find the following declaration.

```
<!--
<Connector port="8443" protocol="HTTP/1.1" SSLEnabled="true"
    maxThreads="150" scheme="https" secure="true"
    clientAuth="false" sslProtocol="TLS" />
-->
```

Uncomment it and make the following changes.

```
<Connector SSLEnabled="true" acceptCount="100" clientAuth="false"
    disableUploadTimeout="true" enableLookups="false" maxThreads="25"
    port="8443" keystoreFile="mykeystore" keystorePass="password"
    protocol="org.apache.coyote.http11.Http11NioProtocol" scheme="https"
    secure="true" sslProtocol="TLS" />
```

```keystoreFile``` and ```keystorePass``` declarations were added, and the ```protocol``` declaration was changed. 

**N.B.** ```keystorePass="password"``` is the password you assigned to your keystore via ```keytool``` command.

**N.B.** In this example ```mykeystore``` keystore file is stored under Tomcat's home directory (```$TOMCAT```).

#### 3. Test it

Start Tomcat and try to access ```https://localhost:8443```. The default ```8080``` port is still working too ```http://localhost:8080```. 

#### 4. How to configure web applications for SSL

Current configuration allows connecting to applications with both HTTP and HTTPS. Restricting access to HTTPS only can be done by adding the following lines at the end of the ```web.xml``` file of an application.

```
<security-constraint>
    <web-resource-collection>
        <web-resource-name>securedapp</web-resource-name>
        <url-pattern>/*</url-pattern>
    </web-resource-collection>
    <user-data-constraint>
        <transport-guarantee>CONFIDENTIAL</transport-guarantee>
    </user-data-constraint>
</security-constraint>
```
