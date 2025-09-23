FROM ubuntu:24.04 AS build
WORKDIR /app

COPY src ./src

ARG ADAPTER_UID=1000
ARG ADAPTER_GID=1000

# copy gradle
COPY build.gradle.kts settings.gradle.kts gradlew ./
COPY gradle/wrapper/gradle-wrapper.jar ./gradle/wrapper/gradle-wrapper.jar
COPY gradle/wrapper/gradle-wrapper.properties ./gradle/wrapper/gradle-wrapper.properties
COPY gradle/libs.versions.toml ./gradle/libs.versions.toml

RUN chmod +x ./gradlew

RUN apt-get update \
    && apt-get install -y openjdk-21-jdk-headless --no-install-recommends \
    && apt-get clean

RUN ./gradlew clean build -x test -x checkstyleMain -x checkstyleTest -x licenseMain -x licenseTest

FROM alpine:3.22.1 AS runtime

ARG ADAPTER_UID=1000
ARG ADAPTER_GID=1000

RUN addgroup -g ${ADAPTER_GID} adapter && \
    adduser -G adapter -u ${ADAPTER_UID} -D adapter && \
    apk add --no-cache openjdk21-jre-headless

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

USER adapter

ENTRYPOINT ["java", "-DcustomPropertiesDir=config", "-jar", "/app/app.jar"]