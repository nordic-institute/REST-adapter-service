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

### IDE Setup

The project can be imported into different IDEs.
Required steps vary depending on the chosen IDE.
Simple quick start is described for IntelliJ IDEA.

#### IntelliJ IDEA

Opening the project in IDEA.

* Install Lombok plugin and Gradle integration plugin, if you have not done it yet
* File -> New project from existing sources -> Choose gradle.build.kts
* Rest of the options can be left at defaults
* Run the project according to the instructions at project's [README](../README.md)

**N.B.**: Running a gradle task using IntelliJ IDEA GUI, you can modify gradle tasks to set the properties directory. In order to do that, open the menu and chose the task that you want to modify. Right click the task and choose "Modify Run Configuration". In the "Environment variables" field, insert the following:
```REST_ADAPTER_PROPERTIES_DIR=<path to properties dir>```

![IntelliJ modifing gradle task.png](attachment/IntelliJ%20modifing%20gradle%20task.png)
