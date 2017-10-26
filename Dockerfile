# Base java:8
FROM java:8

# Add Rest Gateway war to container
ADD src/target/rest-adapter-service-*.war rest-adapter-service.war

# Entry with exec
ENTRYPOINT exec java $JAVA_OPTS -jar /rest-adapter-service.war

# Expose default port
EXPOSE 8080
