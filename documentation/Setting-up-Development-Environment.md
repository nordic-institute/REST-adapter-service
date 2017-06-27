This document describes how a developer's workstation can be setup.

### Software Requirements

* Linux or Windows
* Java 7
* Tomcat 6 or 7 or 8
* Maven 3.x

### Getting the code

There are several of ways to get code, e.g. download it as a [zip](https://github.com/vrk-kpa/REST-adapter-service/archive/master.zip) file or clone the git repository.

```
git clone https://github.com/vrk-kpa/REST-adapter-service.git
```

The code is located in the ```src``` folder.

### Building the code

Rest Adapter Service uses Maven as the build management tool. In order to build the whole project and generate the war file (rest-adapter-service-xxx.war), you must run the maven command below from the ```src``` directory.

```
mvn clean install
```

Running the above maven command generates the war file under the directory presented below:

```
src/target/rest-adapter-service-xxx.war
```

#### Error on building the code

If running ```mvn clean install``` generates the error presented below, there are two possible solutions.

```
[ERROR] Failed to execute goal on project rest-adapter-service: Could not resolve dependencies for project com.pkrete.xrd4j.tools:rest-gateway:war:0.0.3-SNAPSHOT: Failed to collect dependencies at com.pkrete.xrd4j:common:jar:0.0.1: Failed to read artifact descriptor for com.pkrete.xrd4j:common:jar:0.0.1: Could not transfer artifact com.pkrete.xrd4j:common:pom:0.0.1 from/to csc-repo (https://maven.csc.fi/repository/internal/): sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target -> [Help 1]
```

##### Solution 1

Skip certificate validation:

```
mvn install -Dmaven.wagon.http.ssl.insecure=true -Dmaven.wagon.http.ssl.allowall=true
```

##### Solution 2

Import CSC's Maven repository's certificate as a trusted certificate into ```cacerts``` keystore. See full [instructions](Import-a-Certificate-as-a-Trusted-Certificate.md). CSC's Maven repository's URL is ```https://maven.csc.fi```.

### IDE Setup

The project can be imported into different IDEs, but currently this section covers only Netbeans. However, some modifications are required regardless of the IDE that's being used.

#### Netbeans

Opening the project in Netbeans.

* File -> Open Project -> path of the src folder -> Click Open Project button

Adding a new Tomcat server.

* Tools -> Servers -> Add Server

### Running the application

Below there are the default URLs of Provider and Consumer endpoints if the application is run in Netbeans (Run -> Run Project).

* Consumer
  * [http://localhost:8080/RESTGateway/Consumer](http://localhost:8080/RESTGateway/Consumer)
* Provider
  * [http://localhost:8080/RESTGateway/Provider](http://localhost:8080/RESTGateway/Provider)

**N.B.!** If ```rest-adapter-service-xxx.war``` file is manually copied in Tomcat's ```webapp``` folder, then the application can be accessed at:

* Consumer
  * [http://localhost:8080/rest-adapter-service/Consumer](http://localhost:8080/rest-adapter-service-xxx/Consumer)
* Provider
  * [http://localhost:8080/rest-adapter-service/Provider](http://localhost:8080/rest-adapter-service-xxx/Provider)

If you want to test the application by sending requests from Consumer to Provider inside a single instance, then you must update the ```ss.url``` property in ```src/main/resources/consumer-gateway.properties``` file, if the application URL is not ```http://localhost:8080/RESTGateway```. ```ss.url``` property defines the URL where Consumer sends the requests and by default its value is ```http://localhost:8080/RESTGateway/Provider```.
