This document describes how a developer's workstation can be setup.

### Software Requirements

* Linux or Windows
* Java 8
* Maven 3.x

### Getting the code

There are several of ways to get code, e.g. download it as a [zip](https://github.com/nordic-institute/REST-adapter-service/archive/master.zip) file or clone the git repository.

```
git clone https://github.com/nordic-institute/REST-adapter-service.git
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

The project can be imported into different IDEs. 
Required steps vary depending on the chosen IDE. 
Simple quick start is described for IntelliJ IDEA. 

#### IntelliJ IDEA

Opening the project in IDEA.

* Install Lombok plugin and Maven integration plugin, if you have not done it yet
* File -> New project from existing sources -> Choose pom.xml
* Rest of the options can be left at defaults
* Run maven task spring-boot:run to start up Rest Adapter

Once started up, Rest Adapter landing page can be found at
`http://localhost:8080/rest-adapter-service/`
and will contain links to Consumer and Provider endpoints in addition to the Provider wsdl.
