# (in the directory which contains pom.xml)
$ mvn clean package
$ docker build -t rest-adapter-rpm src/main/docker
$ build-rpm-in-docker
