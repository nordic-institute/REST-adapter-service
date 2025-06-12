FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY ./adapter/build.gradle.kts ./adapter/settings.gradle.kts ./adapter/gradlew ./
COPY ./adapter/gradle/wrapper/ ./gradle/wrapper/
COPY ./adapter/gradle/libs.versions.toml ./gradle/
COPY ./adapter/src ./src

RUN chmod +x ./gradlew

RUN ./gradlew clean build -x test -x checkstyleMain -x checkstyleTest -x licenseMain -x licenseTest
RUN cd ./build/libs && ls -lah

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-DcustomPropertiesDir=config", "-jar", "/app/app.jar"]