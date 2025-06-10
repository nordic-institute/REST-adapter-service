# Configuring REST Adapter Service


## Introduction
To enable communication between service providers and consumers, the REST Adapter Service requires specific configuration files. These files define endpoints, encryption settings, namespaces and further configuration for the service.

### Configuration Files Overview
<table>
  <thead>
    <tr>
      <th>File Name</th>
      <th>Description</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <td>providers.properties</td>
      <td>Defines provider endpoints and their settings.</td>
    </tr>
    <tr>
      <td>provider-gateway.properties</td>
      <td>General gateway settings for provider.</td>
    </tr>
    <tr>
      <td>consumers.properties</td>
      <td>Defines consumer endpoints and their settings.</td>
    </tr>
    <tr>
      <td>consumer-gateway.properties</td>
      <td>General gateway settings for consumer.</td>
    </tr>
  </tbody>
</table>

For more information on the configuration files, refer to 
- [Configuring-REST-adapter-service-provider](Configuring-REST-adapter-service-provider.md)
- [CRUD-API-Configuration](CRUD-API-Configuration.md)

## File Locations
In order for REST Adapter Service to work, the configuration must be provided at application startup. This can either be done by placing the configuration files in the default directory or by specifying a custom directory.

1. By default the application will try to find the properties files in the directory where the application is started from.
2. To use a custom directory and overwrite the default location, set the environment variable `CUSTOM_PROPERTIES_DIR` to the directory where the properties files are placed in.
3. With the highest priority, REST-adapter-service will check for a system property `customPropertiesDir` and uses this directory, if set.
    To set the System property, you can use the following command to run the `jar` file:
    ```
     java -DcustomPropertiesDir=<path to properties dir> -jar <path to application>/rest-adapter-service-x.x.x.jar
    ```
   or this command to run the `bootRun` task in Gradle:
    ```
     ./gradlew bootRun -PcustomPropertiesDir=<path to properties dir>
    ```

## Setup example configuration

#### Running REST-adapter-service with example configuration
To test run the application with example configuration, you can copy either the configuration in `adapter/exampleProperties/encrypted` or in `adapter/exampleProperties/plaintext` **to your own directory** and manually replace the placeholders `@projectDir@` and `@rest.adapter.profile.port@` with the actual values.

#### Integration Tests
Running integration tests, you can either provide the commandline argument `-PcustomPropertiesDir=<path to properties dir>` or set the environment variable `CUSTOM_PROPERTIES_DIR` to the desired path. By default, the application will try to load properties files from `exampleProperties/plaintext` directory, automatically replacing `@projectDir@` and `@rest.adapter.profile.port@` placeholders with the actual values. 
To run the integration tests with encrypted example configuration, you can use the following command at `./adapter`:
```
./gradlew intTest -PcustomPropertiesDir=exampleProperties/encrypted/
```

## FAQs
- What happens if a file is missing?

   Startup will fail with an error indicating the missing file.

- How to debug encryption issues?
   
