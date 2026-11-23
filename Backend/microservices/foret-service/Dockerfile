FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="ReboisTN <contact@reboistn.tn>"
LABEL description="Foret Service - Microservice de gestion des zones forestières"

WORKDIR /app

# Copier le jar buildé
COPY target/foret-service-*.jar app.jar

# Variables d'environnement avec valeurs par défaut
ENV DB_USER=postgres
ENV DB_PASSWORD=postgres
ENV EUREKA_URI=http://discovery-server:8761/eureka/

EXPOSE 8081

ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Deureka.client.service-url.defaultZone=${EUREKA_URI}", \
  "-jar", "app.jar"]
