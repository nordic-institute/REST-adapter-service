FROM centos:7
MAINTAINER "Janne Mattila" <janne.mattila@gofore.com>

RUN yum -y install sudo rpm-build java-1.8.0-openjdk-headless maven
RUN yum clean all
RUN sed -i 's/requiretty/!requiretty/' /etc/sudoers

USER root
CMD /usr/bin/mvn -f workspace/pom.xml clean package -P package-rpm

