This document describes how a developer's workstation can be setup.

### Software Requirements

* Linux or Windows
* Java 21
* Gradle 8.x

### Getting the code

There are several of ways to get code, e.g. download it as a [zip](https://github.com/nordic-institute/REST-adapter-service/archive/master.zip) file or clone the git repository.

```
git clone https://github.com/nordic-institute/REST-adapter-service.git
```

The code is located in the ```adapter``` folder.

### Building the code

Rest Adapter Service uses Gradle as the build management tool. In order to build the whole project and generate the jar file (rest-adapter-service-x.x.x.jar), you must run the gradle command below from the ```adapter``` directory.

```
 ./gradlew build
```

Running the above gradle command generates the jar file under the directory presented below:

```
adapter/build/libs/rest-adapter-service-x.x.x.jar
```

### Enabling encryption

The project has two profiles: encrypted and plaintext. The default profile is plaintext. To switch to the encrypted profile, you need to add the property ```encrypted```. This can be done using ```-Pencrypted``` when running the gradle command.  

By doing this, the adapter uses ```main/profiles/encrypted``` as the configuration directory otherwise the directory ```main/profiles/plaintext``` is used.


### IDE Setup

The project can be imported into different IDEs. 
Required steps vary depending on the chosen IDE. 
Simple quick start is described for IntelliJ IDEA. 

#### IntelliJ IDEA

Opening the project in IDEA.

* Install Lombok plugin and Gradle integration plugin, if you have not done it yet
* File -> New project from existing sources -> Choose gradle.build.kts
* Rest of the options can be left at defaults
* Run gradle task ./gradlew bootRun to start up Rest Adapter

Once started up, Rest Adapter landing page can be found at
`http://localhost:8080/rest-adapter-service/`
and will contain links to Consumer and Provider endpoints in addition to the Provider wsdl.
