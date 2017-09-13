# Joint X-Road Rest Adapter Service development  
 
This repository will be the home of REST/JSON support solutions for [X-Road](https://github.com/ria-ee/X-Road). Mandate for joint development is based on [MoU which was signed by Katainen and Ansip](https://github.com/vrk-kpa/REST-adapter-service/blob/master/MoU-Ansip-Katainen.md). The development and repository is shared between Estonia and Finland. Below is list of people who initiated the co-operation in Skype meeting which was held 18.12.2014.

People involved in initiation of co-operation:
* Andres Kütt (Estonian Information System Authority, RIA, Estonia)
* Alar Jõeste (Cybernetica, Estonia)
* Margus Freudenthal (Cybernetica, Estonia)
* Petteri Kivimäki (Population Register Centre, Finland)
* Jarkko Moilanen (Ministry of Education and Culture, Finland)

## Data Exchange Layer X-Road

[X-Road](https://github.com/ria-ee/X-Road) was launched in 2001. The data exchange layer X-Road is a technical and organizational environment, which enables secure Internet-based data exchange between the state’s information systems.

X-Road is not only a technical solution, the exchange of data with the databases belonging to the state information system and between the databases belonging to the state information system shall be carried out through the data exchange layer of the state information system. X-Road allows institutions/people to securely exchange data as well as to ensure people’s access to the data maintained and processed in state databases.

Public and private sector enterprises and institutions can connect their information system with X-Road. This enables them to use X-Road services in their own electronic environment or offer their e-services via X-Road. Joining X-Road enables institutions to save resources, since the data exchange layer already exists. This makes data exchange more effective both inside the state institutions as well as regarding the communication between a citizen and the state.

## Aim 2015
In this repository you will find (2015) Proof of Concept level code for service that will enable REST support in [X-Road](https://github.com/ria-ee/X-Road) version 6. The solution will not be part of Security Server, but more like a "REST Proxy". The solution could be included to Security Server in the future, but that remains to be seen. No plans for that have been made.

First aim is to get first practical REST API integrated to X-Road, document the process and open the code. We also need to test and evaluate the toolchain for example for WSDL-RAML conversions and other things.

In parallel with the technical development we will collect more use cases from Finland about REST/JSON APIs that need to be integrated with X-Road. Aim is not to make automated solution which covers 100% of cases. We will cheer loudly if 80% coverage is achieved.

## Try It Out

The fastest and easiest way to try out the application is to [download](https://github.com/educloudalliance/xroad-rest-gateway/releases/download/v0.0.10/rest-gateway-0.0.10.jar) the executable jar version (```rest-gateway-0.0.10.jar```) and run it: ```java -jar rest-gateway-0.0.10.jar```. The application is accessible at:

```
http://localhost:8080/rest-gateway-0.0.10/Provider

http://localhost:8080/rest-gateway-0.0.10/Consumer
```

The Provider WSDL description is accessible at:

```
http://localhost:8080/rest-gateway-0.0.10/Provider?wsdl
```

## Configuration files location

Rest Adapter Service tries to load configuration files from the following locations

* The directory specified by the system property ```propertiesDirectory```
    ```
    java -jar -DpropertiesDirectory=/my/custom/path rest-adapter-service.jar
    ```
* The directory rest-adapter-service in the user home directory (if it exists)
* The directory /etc/rest-adapter-service (if it exists, Linux only)
* As a fallback, the default configuration shipped with the JAR/WAR (classpath)

More detailed usage examples are available in [documentation](documentation/Rest-Adapter-Service-principles.md#usage).

## Running the Docker Image

Rest Adapter Service is available as Docker image.

```
docker run -p 8080:8080 petkivim/xroad-rest-gateway
```

If customized properties are used, the host directory containing the properties files must be mounted as a data directory. In addition, the directory containing the properties files inside the container must be set using ```JAVA_OPTS``` and```propertiesDirectory``` property.

```
docker run -p 8080:8080 -v /host/dir/conf:/my/conf -e "JAVA_OPTS=-DpropertiesDirectory=/my/conf"  petkivim/xroad-rest-gateway
```

## Building the Docker Image

While you are in the project root directory, build the image using the docker build command. The ```-t``` parameter gives your image a tag, so you can run it more easily later. Don’t forget the ```.``` command, which tells the docker build command to look in the current directory for a file called Dockerfile.

```
docker build -t rest-adapter-service .
```

## Source code license headers

The build uses [license-maven-plugin](https://github.com/mycila/license-maven-plugin) to generate proper license headers for the source code files.

`mvn license:format` generates the license headers where they are missing. More details can be found from the plugin documentation.

## DEB Packaging

The Rest Adapter Service builds DEB package for use with Ubuntu and siblings using the [jdeb Maven plugin](https://github.com/tcurdt/jdeb).

`mvn -f src/pom.xml clean package`

The resulting package depends on tomcat. On installation the war archive is put under Tomcat's webapps directory. Note that when building snapshot versions (i.e. `pom.xml` version string contains `SNAPSHOT`) the resulting package will contain a timestamp to make upgrading existing packages easy.

## RPM Packaging

The Rest Adapter Service also builds RPMs for use with RHEL (or derivatives) and Apache Tomcat using the [rpm-maven-plugin](https://github.com/mojohaus/rpm-maven-plugin).

```mvn -f src/pom.xml clean package```

The resulting rest-adapter-service package depends on tomcat that needs to be preinstalled. Installing the rest-adapter-service RPM package puts rest-adapter-service WAR under Tomcat's webapps directory. Note that when building snapshot versions (i.e. `pom.xml` version string contains `SNAPSHOT`) the resulting package will contain a timestamp to make upgrading existing packages easy.

## Encryption of Message Content

Starting from version 0.0.10 Rest Adapter Service supports encryption/decryption of message content. More information and instructions for configuration can be found in [documentation](documentation/Encryption.md).

By default plaintext configuration is enabled. The software can be built with encryption configuration enabled using the command below.

```mvn clean install -P encrypted```

Running integration tests with plaintext configuration enabled:

```mvn clean install -P itest -P plaintext```

Running integration tests with encryption configuration enabled:

```mvn clean install -P itest -P encrypted```

### Additional documentation

* [Requirements](documentation/Requirements.md)
* [Setting up Development Environment](documentation/Setting-up-Development-Environment.md)
* [Setting up SSL on Tomcat](documentation/Setting-up-SSL-on-Tomcat.md)
* [Import a Certificate as a Trusted Certificate](documentation/Import-a-Certificate-as-a-Trusted-Certificate.md)
* [Encryption](documentation/Encryption.md)
* [Rest Adapter Service principles](documentation/Rest-Adapter-Service-principles.md)
* Examples
  * [Configuring Rest Adapter Service provider](documentation/Configuring-Rest-Adapter-Service-provider.md)
  * [CRUD API Configuration](documentation/CRUD-API-Configuration.md)

### Links to material

* [Data Exchange Layer X-Road](https://www.ria.ee/x-road/)
* [X-Road overview ](https://speakerdeck.com/educloudalliance/x-road-overview)
* [X-Road regulations](https://speakerdeck.com/educloudalliance/x-road-regulations)
* [Palveluväylä kehitysympäristö (Finnish only)](http://palveluvayla.fi)
* [Requirements for Information Systems and Adapter
Servers](http://x-road.ee/docs/eng/x-road_service_protocol.pdf)
* [XRd4J - Java Library for X-Road v6](https://github.com/petkivim/xrd4j)
