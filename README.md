# X-Road REST Adapter Service

[![Go to X-Road Community Slack](https://img.shields.io/badge/Go%20to%20Community%20Slack-grey.svg)](https://jointxroad.slack.com/)
[![Get invited](https://img.shields.io/badge/No%20Slack-Get%20invited-green.svg)](https://x-road.global/community)

REST Adapter Service provides REST support to [X-Road](https://github.com/nordic-institute/X-Road) data exchange layer solution. REST Adapter Service sits between [X-Road](https://github.com/nordic-institute/X-Road) Security Server and a REST client or service ([diagram](https://github.com/nordic-institute/REST-adapter-service/raw/master/images/message-sequence_rest-gateway-0.0.4.png)). The component implements X-Road v4.0 [SOAP profile](https://github.com/nordic-institute/X-Road/blob/develop/doc/Protocols/pr-mess_x-road_message_protocol.md) and it's compatible with X-Road v6.4 and above.

REST Adapter Service has two parts: _Consumer Gateway_ and _Provider Gateway._ It is possible to use either only Consumer Gateway, only Provider Gateway, or both.

![different adapter usage scenarios](images/restgw-use-cases.png "Different adapter usage scenarios")

* (A) using both Consumer and Provider Gateways
  * when both the client and the server are REST/JSON, but the messages need to go through X-Road
  * when end to end encryption is needed
* (B) using only Consumer Gateway
  * when the service is SOAP/XML, but a client needs to access it through REST/JSON
* (C) using only Provider Gateway
  * when a REST/JSON service needs to be published in X-Road for SOAP/XML clients

More information about available features can be found [here](documentation/Rest-Adapter-Service-principles.md).

## Try It Out

The fastest and easiest way to try out the application is by using the Spring Boot Maven plugin.
To do this, you need to have a working installation of [Gradle](https://gradle.org/).


```
cd adapter
./gradlew bootRun
```
After that you can access `http://localhost:8080/rest-adapter-service/` to see the Rest Adapter landing page.

If customized location for ```ConsumerGateway``` and ```ProviderGateway``` properties are used, use the following 
syntax to define 
```
./gradlew bootRun --PpropertiesDirectory=/my/conf
```

When using default properties file location (classpath) you can pass ```-Pencrypted``` to use the encrypted-profiles 
properties file. If that property is not provided, plaintext-profile will be used.
```
./gradlew bootRun --Pencryped
```

## Configuration files location

Rest Adapter Service tries to load configuration files from the following locations,
in the following order.

If a matching directory exists, all the configuration files
need to exist in that directory, otherwise an error occurs. Configuration
directory scanning stops once the first matching directory is located.

Scanned directories:
1. The directory specified by the system property ```propertiesDirectory```
    ```
    java -jar -DpropertiesDirectory=/my/custom/path rest-adapter-service-x.x.x.jar
    ```
2. As a fallback, the default configuration shipped with the WAR (classpath)


More detailed usage examples are available in [documentation](documentation/Rest-Adapter-Service-principles.md#usage).

# Installing Rest Adapter Service

Build or download the jar file. You need to have Java 21 installed. Run 
```shell
java -jar rest-adapter-service-x.x.x.jar
```

### Changing the port
To change the port, modify configuration file `/etc/rest-adapter-service/application.properties`
```shell
# change this to customize port
server.port=8080
```

# Building and packaging

## Source code license headers

The build uses [license-gradle-plugin](https://github.com/hierynomus/license-gradle-plugin) to generate proper license headers for the source code files.

`./gradlew licenseMain` generates the license headers where they are missing. More details can be found from the plugin documentation.

## Building docker container
From ```/adapter``` run
```shell
# (in the directory which contains pom.xml)
docker build -t rest-adapter-service .
#./build-rpm-in-docker.sh
```

## Encryption of Message Content

Starting from version 0.0.10 Rest Adapter Service supports encryption/decryption of message content. More information and instructions for configuration can be found in the [documentation](documentation/Encryption.md).

By default plaintext configuration is enabled. The software can be built with encryption configuration enabled using the command below.
This setting only affects the default configuration bundled inside the war file, and the integration tests.
External configuration, in `/etc/rest-adapter-service`
or elsewhere, is not affected.

```./gradlew clean build -Pencrypted```

Running integration tests with plaintext configuration enabled:

```./gradlew clean intTest```

Running integration tests with encryption configuration enabled:

```./gradlew clean intTest -Pencrypted```

Integration tests are run on port `9898`

## Mocking external API's for integration tests

Integration tests execute requests against several external API's, such as `http://www.hel.fi/palvelukarttaws/rest/v4/organization/`.
These external API's may for example suffer from temporary downtime, or have their data changed so that integration tests no longer pass.

### Additional documentation

* [Requirements](documentation/Requirements.md)
* [Setting up development environment](documentation/Setting-up-Development-Environment.md)
* [Setting up SSL on Tomcat](documentation/Setting-up-SSL-on-Tomcat.md)
* [Import a certificate as a trusted certificate](documentation/Import-a-Certificate-as-a-Trusted-Certificate.md)
* [Encryption](documentation/Encryption.md)
* [Rest Adapter Service principles](documentation/Rest-Adapter-Service-principles.md)
* [Obtaining WAR file from package repository](documentation/Downloading-WAR-from-repository.md)
* Examples
  * [Configuring Rest Adapter Service provider](documentation/Configuring-Rest-Adapter-Service-provider.md)
  * [CRUD API configuration](documentation/CRUD-API-Configuration.md)

### Links to material

* [Data exchange layer X-Road](https://github.com/nordic-institute/X-Road)
* [X-Road community](https://x-road.global/)
* [Nordic Institute for Interoperability Solutions (NIIS)](https://www.niis.org/)
* [XRd4J - Java library for X-Road v6](https://github.com/nordic-institute/xrd4j)

## Want to contribute?

For more information look at the [contribution instructions](CONTRIBUTING.md).

## Credits

The development of REST Adapter Service started as a joint effort between Finland and Estonia in December 2014. Since then the component has been developed by different people and organizations.

* REST Gateway was originally developed by the people listed below (https://github.com/educloudalliance/xroad-rest-gateway) during 2014-2017.
* The name of the component was changed to REST Adapter Service (https://github.com/vrk-kpa/REST-adapter-service) and it was maintained and further developed by the Finnish Population Register Centre (VRK) during 06/2017-05/2018.
* In June 2018 it was agreed that Nordic Institute for Interoperability Solutions (NIIS) takes maintenance responsibility.

Below is a list of people who initiated the co-operation in a Skype meeting which was held on 18th December 2014:

* Andres Kütt (Estonian Information System Authority, RIA, Estonia)
* Alar Jõeste (Cybernetica, Estonia)
* Margus Freudenthal (Cybernetica, Estonia)
* Petteri Kivimäki (Population Register Centre, Finland)
* Jarkko Moilanen (Ministry of Education and Culture, Finland)
