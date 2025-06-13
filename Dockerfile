FROM ubuntu:24.04 AS build
WORKDIR /app

COPY ./adapter/build.gradle.kts ./adapter/settings.gradle.kts ./adapter/gradlew ./
COPY ./adapter/gradle/wrapper/ ./gradle/wrapper/
COPY ./adapter/gradle/libs.versions.toml ./gradle/
COPY ./adapter/src ./src

RUN chmod +x ./gradlew

RUN apt-get update \
    && apt-get install -y openjdk-21-jdk \
    && apt-get clean

RUN ./gradlew clean build -x test -x checkstyleMain -x checkstyleTest -x licenseMain -x licenseTest
RUN cd ./build/libs && ls -lah

FROM ubuntu:24.04
WORKDIR /app

RUN apt-get update \
    && apt-get install -y openjdk-21-jre \
    && apt-get clean

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-DcustomPropertiesDir=config", "-jar", "/app/app.jar"]