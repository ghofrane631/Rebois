FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/ms-coordination-crise-*.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
