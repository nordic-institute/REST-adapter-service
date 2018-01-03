# Change Log

## 0.0.13-SNAPSHOT 2017-12-20
- Added new folder for templates and an example AWS CloudFormation template for testing REST Adapter Service.
- PVAYLADEV-1001 Improved Rest Adapter Service documentation in relation to Docker.
- PVAYLADEV-943 Added instructions for obtaining war package from a package repository.
- PVAYLADEV-1013 Fixed a potential security problem, convertPost = true in combination with encrypted = true did leave converted message unencrypted in the message body.
- PVAYLADEV-940 Changed into a Spring Boot application. Installation package no longer installs a standalone Tomcat server. Instead the Adapter is run as a self contained Java application, with an embedded web container. Added packaging for Debian with Systemd init system (= e.g. Ubuntu 16).

