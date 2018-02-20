FROM centos:7
MAINTAINER "Janne Mattila" <janne.mattila@gofore.com>

ENV LANG en_US.UTF-8
ENV LC_ALL en_US.UTF-8
ENV JAVA_HOME /etc/alternatives/java_sdk_1.8.0_openjdk

RUN yum install -y sudo rpm-build wget java-1.8.0-openjdk-devel

RUN cd /usr/local && wget -q http://www-eu.apache.org/dist/maven/maven-3/3.5.2/binaries/apache-maven-3.5.2-bin.tar.gz && tar xzf apache-maven-3.5.2-bin.tar.gz && ln -s apache-maven-3.5.2 maven

RUN yum clean all
RUN sed -i 's/requiretty/!requiretty/' /etc/sudoers

USER root
CMD /usr/local/maven/bin/mvn -f workspace/pom.xml clean install -P package-rpm -B -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=warn
