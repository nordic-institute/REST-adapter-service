FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
ARG PROPS_DIR_HOST
ARG CUSTOM_PROPERTIES_DIR=./config

COPY ./adapter/build.gradle.kts ../adapter/settings.gradle.kts ../adapter/gradlew ./
COPY ./adapter/gradle/wrapper/ ./gradle/wrapper/
COPY ./adapter/gradle/libs.versions.toml ./gradle/
COPY ./adapter/src ./src

# Copy the properties and keys meant for encrypted mode. Use directory provided by user's ARG or default.
COPY $PROPS_DIR_HOST $CUSTOM_PROPERTIES_DIR
RUN chmod +x ./gradlew
ENTRYPOINT [ "sh" ]

RUN ./gradlew clean build -x test -x checkstyleMain -x checkstyleTest -x licenseMain -x licenseTest --info

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# This needs to be the same like set in the consumer-gateway.properties and provider-gateway.properties specified
ARG ENCRYPTION_KEYS_DIR=/app/build/resources/main/resources-bin/

# Copy the built JAR from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar
COPY ./adapter/src/main/resources-bin $ENCRYPTION_KEYS_DIR

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]