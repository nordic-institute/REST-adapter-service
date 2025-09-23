# Configuring REST Adapter Service


## Introduction
To enable communication between service providers and consumers, the REST Adapter Service requires specific configuration files. These files define endpoints, encryption settings, namespaces and further configuration for the service.

### Configuration Files Overview
| File Name                  | Description                                |
|----------------------------|--------------------------------------------|
| providers.properties       | Defines provider endpoints and their settings. |
| provider-gateway.properties| General gateway settings for provider.     |
| consumers.properties       | Defines consumer endpoints and their settings. |
| consumer-gateway.properties| General gateway settings for consumer.     |

For more information on the configuration files, refer to 
- [Configuring-REST-adapter-service-provider](Configuring-REST-adapter-service-provider.md)
- [CRUD-API-Configuration](CRUD-API-Configuration.md)

## File Locations
In order for REST Adapter Service to work, the configuration must be provided at application startup. This can either be done by placing the configuration files in the default directory or by specifying a custom directory.

1. By default the application will try to find the properties files in the directory where the application is started from.
2. To use a custom directory and overwrite the default location, set the environment variable `REST_ADAPTER_PROPERTIES_DIR` to the directory where the properties files are placed in.
3. With the highest priority, REST-adapter-service will check for a system property `customPropertiesDir` and uses this directory, if set.
    To set the System property, you can use the following command to run the `jar` file:
    ```bash
     java -DcustomPropertiesDir=<path to properties dir> -jar <path to application>/rest-adapter-service-x.x.x.jar
    ```
   or this command to run the `bootRun` task in Gradle:
    ```bash
     ./gradlew bootRun -PcustomPropertiesDir=<path to properties dir>
    ```

## Setup example configuration

### Running REST-adapter-service with example configuration
To test run the application with example configuration, you can copy the `./adapter/exampleProperties/*.properties.example` as `.properties` files into `<path to properties dir>` and replace the placeholders. 

### Integration Tests
Integration tests can be run plaintext or encrypted. Plaintext is run by default, if you do not provide a commandline argument like `-PcustomPropertiesDir=<path to properties dir>` or set the environment variable `REST_ADAPTER_PROPERTIES_DIR` to the desired path. 
Resourec processing will automatically replace `@projectDir@` and `@rest.adapter.profile.port@` placeholders in the properties files with the actual values. 
To run the integration tests with encrypted configuration, you can use the following command from `./adapter` directory:
```bash
./gradlew intTest -PcustomPropertiesDir=src/test/resources/application-intTest-properties/encrypted
```

### Running REST-adapter-service using Docker 
To run the REST Adapter Service using Docker with example configuration, please follow these steps:
1. Please copy the example configuration files from `adapter/exampleProperties` as `.properties` files to `<path to properties dir>` (can be the same folder) and replace all placeholders. 
2. If you need to build the Docker image, you can use this command
   ```bash
   docker build -t rest_adapter_service .
   ```
3. Run the Docker container with the following command, replacing `<path to properties dir>`, `<path to keystores dir>` and `<docker path to keystores>` with the actual paths. The mounted path in Docker container `<docker path to keystores>` needs to match the path that is referenced from the properties files, e.g. `publicKeyFile` in `provider-gateway.properties`:
   ```bash
   docker run --name rest_adapter_service \
      -p 8080:8080 \
      -v <path to properties dir>:/app/config:ro \
      -v <path to keystores dir>:<docker path to keystores>:ro \
      rest_adapter_service
    ```
   This will mount your properties directory into the container `/app/config` and start the REST Adapter Service with the provided configuration.
      
   **N.B.!** If you want to add a wsdl file to the container, please add this volume `-v <path to wsdl file>:<docker path to wsdl file>:ro \` to the command and replace the placeholders. `<docker path to wsdl file>` needs to be the same path that is referenced in `provider-gateway.properties` file in `wsdl.path`.

   
