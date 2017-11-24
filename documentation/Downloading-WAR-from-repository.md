If you do not want to install Rest Adapter as a standalone service from package repository, you can 
obtain the war package from package repository, and for example deploy it to a standalone Tomcat installation.

To do this, you can either

1. install Rest Adapter service from package repository to some server or container, and copy `/usr/lib/rest-adapter-service/rest-adapter-service.war` from there 
2. extract `rest-adapter-service.war` from the installation packages, using commands below

The commands listed below are examples, and you may need to modify them depending on your exact system configuration.
The commands were tested using `root` user inside a LXC container - in a real system you need to use `sudo`
with many of the commands.

# Extract rest-adapter-service.war from Ubuntu .deb packages

1. configure package repository
2. `apt-get update`
3. download the package to current directory
```shell
apt-get download rest-adapter-service
``` 
4. extract `rest-adapter-service.war` from the package (use actual .deb filename)
```shell
dpkg-deb --fsys-tarfile rest-adapter-service_0.0.13~20171120080044_all.deb | tar -x ./usr/lib/rest-adapter-service/rest-adapter-service.war
``` 
5. use the extracted `rest-adapter-service.war`
```shell
ls -la ./usr/lib/rest-adapter-service/rest-adapter-service.war 
-rw-r----- 1 root root 22458761 Nov 20 06:00 ./usr/lib/rest-adapter-service/rest-adapter-service.war
``` 

# Extract rest-adapter-service.war from RedHat .rpm packages

1. configure package repository
2. install yumdownloader, if not already installed 
```shell
yum install yum-utils
``` 
3. download the package to current directory
```shell
yumdownloader rest-adapter-service
``` 
4. extract `rest-adapter-service.war` from the package (use actual .rpm filename)
```shell
rpm2cpio rest-adapter-service-0.0.13-SNAPSHOT20171120060123.noarch.rpm | cpio -iv --to-stdout ./usr/lib/rest-adapter-service/rest-adapter-service.war > ./rest-adapter-service.war 
``` 
5. use the extracted `rest-adapter-service.war`
```shell
ls -la rest-adapter-service.war 
-rw-r--r-- 1 root root 22458762 Nov 21 15:24 rest-adapter-service.war
``` 


