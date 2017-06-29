# Base java:8
FROM java:8

# Add Rest Gateway jar to container
ADD src/target/rest-adapter-service-*.jar rest-adapter-service.jar

# Entry with exec
ENTRYPOINT exec java $JAVA_OPTS -jar /rest-adapter-service.jar

# Expose Tomcat
EXPOSE 8080
