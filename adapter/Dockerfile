FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
ARG PROPS_DIR

COPY ./build.gradle.kts ./settings.gradle.kts ./gradlew ./
COPY ./gradle/wrapper/ ./gradle/wrapper/
COPY ./gradle/libs.versions.toml ./gradle/
COPY src ./src
COPY ${PROPS_DIR}/* ./config/

RUN chmod +x ./gradlew
RUN ./gradlew clean build -PpropertiesDirectory=./config -x test -x checkstyleMain -x checkstyleTest -x licenseMain -x licenseTest



FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy the built JAR from the previous stage
COPY --from=build /app/build/libs/*.jar app.jar

# Set the entry point
ENTRYPOINT ["java", "-jar", "app.jar"]