Rest Adapter Service supports message level encryption. The use of encryption requires that service provider and consumer exchange their public keys (provider => ```SPPUB```, consumer => ```SCPUB```) as Rest Adapter Service does not currently offer any mechanism for that. The encryption/decryption process is explained below.

* Consumer Gateway generates one time symmetric AES-128 key ```K1``` that's used for encrypting the message payload.
* ```K1``` is then encrypted using service provider's public key ```SPPUB```.
* Encrypted data, encrypted ```K1``` and initialization vector are added to the message payload.

```
<request>
    <encrypted>nYZYpsPsN+9gqlZScmYsAMQdWUiCFi...EY0e2oPHJWxAbHn4qMzzuC2VmORvhuapEzUsq2+XXkg==</encrypted>
    <key>hTDsRrsowRk+qvV9OnWDvnPHrmb/JUvFRR+t...SKVcX2gnH91NXY/+1kV4A==</key>
    <iv>iVVpqECe1ZLm7LLcGoq7gg==</iv>
</request>
```

* Provider Gateway decrypts the one time symmetric AES-128 key ```K1``` using its private key ```SPPRI```.
* Message data is then decrypted using ```K1``` and initialization vector.
* Provider Gateway then calls the information system using the plain text message data.
* Provider Gateway generates an new one time symmetric AES-128 key ```K2``` that's used for encrypting the response message.
* ```K2``` is then encrypted using service consumer's public key ```SCPUB```.
* Encrypted response, encrypted ```K2``` and initialization vector are added to the response message's payload.

```
<request>
    <encrypted>nYZYpsPsN+9gqlZScmYsAMQdWUiCFi...EY0e2oPHJWxAbHn4qMzzuC2VmORvhuapEzUsq2+XXkg==</encrypted>
    <key>hTDsRrsowRk+qvV9OnWDvnPHrmb/JUvFRR+t...SKVcX2gnH91NXY/+1kV4A==</key>
    <iv>iVVpqECe1ZLm7LLcGoq7gg==</iv>
</request>
<response>
    <ts1:encrypted>poc3v2yBkHar...xIXLQu9kicblCHdyCqnkKgR3M1XtDxZKwQ==</encrypted>
    <key>HpVFE7e6y2JEt8Z/2/4IX9nwEwqLgTYp...bdlxkFaj0WDDBQiGCCurA==</key>
    <iv>ykbXPforotsFQAVSh9wI+A==</iv>
<response>
```

* Consumer Gateway decrypts the one time symmetric AES-128 key ```K2``` using its private key ```SCPRI```.
* Response message's data is then decrypted using ```K2``` and initialization vector.
* Plain text response data is then returned to the client side information system.

## Configuration

* Generate RSA key pair for Consumer Gateway.

```
keytool -genkey -keyalg RSA -alias consumerpri -keystore consumerkeystore.jks -storepass consumerks -validity 360 -keysize 2048
```

* Export the public key.

```
keytool -export -alias consumerpri -keystore consumerkeystore.jks -rfc -file consumer.cer
```

* Generate RSA key pair for Provider Gateway.

```
keytool -genkey -keyalg RSA -alias providerpri -keystore providerkeystore.jks -storepass providerks -validity 360 -keysize 2048
```

* Export the public key.

```
keytool -export -alias providerpri -keystore providerkeystore.jks -rfc -file provider.cer
```

### Consumer Gateway

Import Service Provider's certificate to Service Consumer's trust store. Certificate alias must be the complete service identifier of the service (```FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1```) that is going to be called using this certificate. **NB!** If other services of the same service provider are called using the same certificate, the certificate must be imported separately for each service using the full service identifier as an alias.

```
keytool -import -file provider.cer -alias FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1 -keystore consumertruststore.jks
```

```consumer-gateway.properties``` file must contain the properties below.

```
# Key length (in bits) of symmetric key. Default is 128 bits.
# NB! Longer key requires installing unlimited key file:
# http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
keyLength=128
# Absolute path of the trust store file where public keys are stored
publicKeyFile=/path/to/consumertruststore.jks
# Password of the trust store file
publicKeyFilePassword=consumerts
# Absolute path of the key store file where the private key is stored
privateKeyFile=/path/to/consumerkeystore.jks
# Password of the key store file
privateKeyFilePassword=consumerks
# Alias of the private key
privateKeyAlias=consumerpri
# Password of the private key
privateKeyPassword=consumer
```

Enabling encryption of sent messages and decryption of received responses for each service is enabled in the ```consumers.properties``` file.

```
0.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1
0.id.client=FI_PILOT.GOV.0245437-2.ConsumerTest
0.path=/www.hel.fi/palvelukarttaws/rest/v2/organization/
0.verb=get
# Optional - If set to true, request is encrypted. Default : false
# If value is true, all the settings related to encryption must be defined
# in consumer-gateway.properties file.
0.request.encrypted=true
# Optional - If set to true, expects response to be encrypted. Default : false
# If value is true, all the settings related to encryption must be defined
# in consumer-gateway.properties file.
0.response.encrypted=true
```

### Provider Gateway

Import Service Consumer's public key to Service Provider's trust store. Certificate alias must be the complete service identifier of the subsystem (```FI_PILOT.GOV.0245437-2.ConsumerTest```) that has been granted access to one of the services provided by the Provider Gateway instance. Even if the same subsystem is used for accessing multiple services, it's enough to import the certificate once.

```
keytool -import -file consumer.cer -alias FI_PILOT.GOV.0245437-2.ConsumerTest -keystore producertruststore.jks
```

```provider-gateway.properties``` file must contain the properties below.

```
# Key length (in bits) of symmetric key. Default is 128 bits.
# NB! Longer key requires installing unlimited key file:
# http://www.oracle.com/technetwork/java/javase/downloads/jce8-download-2133166.html
keyLength=128
# Absolute path of the trust store file where public keys are stored
publicKeyFile=/path/to/producertruststore.jks
# Password of the trust store file
publicKeyFilePassword=providerts
# Absolute path of the key store file where the private key is stored
privateKeyFile=/path/to/providerkeystore.jks
# Password of the key store file
privateKeyFilePassword=providerks
# Alias of the private key
privateKeyAlias=providerpri
# Password of the private key
privateKeyPassword=provider
```

Enabling decryption of received requests and encryption of returned responses for each service is enabled in the ```providers.properties``` file.

```
0.id=FI_PILOT.GOV.1019125-0.Demo2Service.getOrganizationList.v1
0.id.client=FI_PILOT.GOV.0245437-2.ConsumerTest
0.path=/www.hel.fi/palvelukarttaws/rest/v2/organization/
0.verb=get
# Optional - If set to true, expects request to be encrypted. Default : false
# If value is true, all the settings related to encryption must be defined
# in provider-gateway.properties file.
0.request.encrypted=true
# Optional - If set to true, response is encrypted. Default : false
# If value is true, all the settings related to encryption must be defined
# in provider-gateway.properties file.
0.response.encrypted=true
```
