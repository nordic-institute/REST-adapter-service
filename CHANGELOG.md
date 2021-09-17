# Change Log

## 1.1.0-SNAPSHOT 2021-09-17
- Add 3rd party notices

## 1.1.0-SNAPSHOT 2020-09-14
- XRDDEV-1347 Update dependencies, migrate from Spring Boot 1.x to 2.x, and from Tomcat 8 to 9

## 1.0.0 2018-07-07
- XRDDEV-91 Fix checkstyle and sonar issues, update hoverfly simulation
- XRDDEV-89 Change xrd4j from snapshot to release
- XRDDEV-45 Change copyright owner from VRK to NIIS and artifact package names from `fi.vrk.xroad.restadapterservice` to `org.niis.xroad.restadapterservice`.
- XRDDEV-38 Change xrd4j dependency from fi.vrk.xr4j to org.niis.xrd4j
- XRDDEV-38 Change Maven download URL to one that does not stop working when new versions are released
- XRDDEV-81 Change integration tests to be more tolerant against changes to external APIs. Add possibility to integration test against Hoverfly simulation. 

## 0.0.13 2018-02-23
- PVAYLADEV-1059 Add checkstyle and sonar checks and fix reported problems
- Added new folder for templates and an example AWS CloudFormation template for testing REST Adapter Service.
- PVAYLADEV-1001 Improved Rest Adapter Service documentation in relation to Docker.
- PVAYLADEV-943 Added instructions for obtaining war package from a package repository.
- PVAYLADEV-1013 Fixed a potential security problem, convertPost = true in combination with encrypted = true did leave converted message unencrypted in the message body.
- PVAYLADEV-940 Changed into a Spring Boot application. Installation package no longer installs a standalone Tomcat server. Instead the Adapter is run as a self contained Java application, with an embedded web container. Added packaging for Debian with Systemd init system (= e.g. Ubuntu 16).

