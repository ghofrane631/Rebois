# ms-signalement — Ghofrane Jridi

Port **8083** · MongoDB `signalement_db` (collection `signalements`) · Eureka `SIGNALEMENT`

## Lancer en local
```bash
# 1. MongoDB + RabbitMQ
docker run -d --name mongo-signalement -p 27017:27017 mongo:7
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3-management
# 2. Le service (profil dev = sécurité ouverte)
mvn spring-boot:run
# 3. Swagger : http://localhost:8083/swagger-ui.html
```

## Tests Postman / curl (profil dev)
```bash
curl -X POST localhost:8083/api/signalements -H "Content-Type: application/json" \
 -d '{"type":"FUMEE","latitude":36.42,"longitude":10.08,"intensite":3,"description":"fumée","photoUrl":"http://x/p1.jpg"}'
curl "localhost:8083/api/signalements/proches?lat=36.42&lng=10.08&rayonM=500"
curl -X PATCH localhost:8083/api/signalements/<id>/confirmer -H "Content-Type: application/json" -d '{"motif":"vu sur place"}'
curl localhost:8083/api/signalements/actifs
```

## Événements (exchange topic `ffw.events`)
| Sens | Routing key | Contenu |
|---|---|---|
| Publie | `feu.confirme` | eventId, signalementId, secteurId, latitude, longitude, intensite, occurredAt |
| Consomme (file `signalement.intervention-terminee`) | `intervention.terminee` | eventId, incidentId, **signalementId**, secteurId, surfaceBruleeHa |

À convenir avec Mezen : que InterventionTerminee contienne `signalementId`
(le secteur seul permet un repli : on clôt les CONFIRME du secteur).
À convenir avec Thouraya : `GET /api/secteurs/localiser?lat=&lng=` renvoie `{ "id": 14, "code": "ZAG-03" }`.

## Plus tard
- S6 : Resilience4j (timeout/retry/circuit breaker) sur ForetClient
- S7 : outbox (OutboxMessage), eventId dédupliqué, retry + dead-letter queue
- S8 : lancer avec `SPRING_PROFILES_ACTIVE=prod` → JWT Keycloak + rôles

## docker-compose (à ajouter dans Backend/docker-compose.yml)
```yaml
  mongo-signalement:
    image: mongo:7
    volumes: [ "mongo_signalement_data:/data/db" ]
    networks: [ ffw-net ]
  ms-signalement:
    build: ./microservices/ms-signalement
    ports: [ "8083:8083" ]
    environment:
      SPRING_PROFILES_ACTIVE: prod
      MONGO_URI: mongodb://mongo-signalement:27017/signalement_db
      RABBIT_HOST: rabbitmq
      EUREKA_URL: http://discovery-server:8761/eureka
      CONFIG_SERVER_URL: http://config-server:8888
      KEYCLOAK_URL: http://keycloak:8180
    depends_on: [ mongo-signalement ]
    networks: [ ffw-net ]
# volumes: mongo_signalement_data:
```
