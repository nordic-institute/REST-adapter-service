# CloudFormation Templates

This folder contains AWS CloudFormation templates for X-Road REST Adapter Service.

##  EC2 REST Adapter Service

**Disclaimer**

This template is NOT officially supported by REST Adapter Service.
This template is only an example of how AWS could be used with REST
Adapter Service. In addition, it is not guaranteed that every version of REST
Adapter Service will work with this AWS template and in the case of issues,
no support is provided by the development team.

```ec2-rest-adapter-service.yaml``` template is for for testing the latest development version of X-Road REST Adapter Service. The template creates a VPC and adds an EC2 instance with an Elastic IP address and a security group. After that the latest version of REST Adapter Service is fetched from GitHub, compiled and started. Once the stack is created the URL of the REST Adapter Service can be checked from the ```Output``` tab. **N.B.** REST Adapter Service is not available immediately when the stack is ready. Installing and configuring REST Adapter Service might take 3-5 minutes.

The template creates the following AWS resources:

* VPC
* Subnet
* EC2 instance (instance type can be selected)
* Security Group
* Network ACL
* Elastic IP
* Route Table

### Parameters

```ec2-rest-adapter-service.yaml``` template has the following parameters which values are defined when a new stack is created.

* InstanceType
  * EC2 instance type.
* KeyName
  * Existing EC2 KeyPair to enable SSH access to the instance. If you don't have
  a key pair yet you must create one before using this template.
* SSHLocation
  * The IP address range that can be used to SSH to the EC2 instance.
  * Must be a valid IP CIDR range of the form x.x.x.x/x.
    * It is not recommended to use ```0.0.0.0/0``` that makes the service accessible anywhere from the internet.
* HTTPAccessLocation
  * The IP address range that can be used to access the defined port.
  * Must be a valid IP CIDR range of the form x.x.x.x/x.
    * It is not recommended to use ```0.0.0.0/0``` that makes the service accessible anywhere from the internet.
* ServerPort
  * The port number that REST Adapter Service is listening (default is 8080).
  * REST Adapter Service is available at ```http://<IP_ADDRESS>:<ServerPort>/rest-adapter-service/```
