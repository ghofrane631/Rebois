# ms-signalement — Ghofrane Jridi

Service de **signalement des incendies de forêt** : création d'un signalement géolocalisé,
détection des doublons proches, rattachement au secteur forestier, confirmation par un garde,
photos et clôture automatique à la fin de l'intervention.

- Port : **8083**
- Base : MongoDB `signalement_db` (collections `signalements`, `outbox`, `processed_events`)
- Eureka : `SIGNALEMENT`
- Exchange RabbitMQ : `ffw.events` (+ `ffw.events.dlx` pour les messages morts)
- Stack : Spring Boot 3.2.5 / Java 17, Spring Data MongoDB, Spring AMQP, OpenFeign + Resilience4j,
  Eureka client, Config client, Keycloak (OAuth2 Resource Server), springdoc-openapi.

## 1. Démarrage

### Tout-en-un (Docker Compose)

Depuis `Backend/` :

```bash
docker compose up -d --build
```

| Service | URL |
|---|---|
| Swagger UI | http://localhost:8083/swagger-ui.html |
| API | http://localhost:8083/api/signalements |
| Eureka | http://localhost:8761 |
| Config Server | http://localhost:8888/signalement/dev |
| Keycloak | http://localhost:8180 (admin / admin) |
| Console RabbitMQ | http://localhost:15672 (guest / guest) |

Le profil `prod` (JWT Keycloak) est actif dans Compose.

### Développement local (profil `dev`, sécurité ouverte)

```bash
docker run -d --name mongo-signalement -p 27017:27017 mongo:7
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.13-management
mvn spring-boot:run
```

Le profil `dev` est le profil par défaut : aucun jeton n'est requis.
Le profil `prod` active la validation JWT et les rôles.

### Obtenir un jeton (profil prod)

```bash
TOKEN=$(curl -s -X POST http://localhost:8180/realms/forest-fire-watch/protocol/openid-connect/token \
  -d grant_type=password -d client_id=ffw-web \
  -d username=garde -d password=garde | jq -r .access_token)
```

Comptes de démonstration : `admin/admin` (ADMIN), `garde/garde` (GARDE), `benevole/benevole` (BENEVOLE).

## 2. API

| Méthode | URI | Rôle | Codes |
|---|---|---|---|
| POST | `/api/signalements` | GARDE, BENEVOLE | 201, 400, 401, 403 |
| GET | `/api/signalements?secteurId=&statut=&page=&size=&sort=creeLe,desc` | authentifié | 200, 400 |
| GET | `/api/signalements/proches?lat=&lng=&rayonM=500` | authentifié | 200, 400 |
| GET | `/api/signalements/actifs` | public | 200 |
| GET | `/api/signalements/{id}` | authentifié | 200, 404 |
| POST | `/api/signalements/{id}/photos` | auteur du signalement | 201, 400, 403, 404 |
| PATCH | `/api/signalements/{id}/confirmer` | GARDE | 200, 404, 409 |
| PATCH | `/api/signalements/{id}/fausse-alerte` | GARDE | 200, 404, 409 |

La recherche est paginée (`Page` Spring) : `content`, `page`, `size`, `totalElements`, `totalPages`.

Exemple (profil dev) :

```bash
curl -X POST localhost:8083/api/signalements -H "Content-Type: application/json" \
 -d '{"type":"FUMEE","latitude":36.42,"longitude":10.08,"intensite":3,"description":"fumée","photoUrl":"https://x/p1.jpg"}'

curl "localhost:8083/api/signalements/proches?lat=36.42&lng=10.08&rayonM=500"
curl "localhost:8083/api/signalements?statut=CONFIRME&page=0&size=20"

curl -X PATCH localhost:8083/api/signalements/<id>/confirmer \
 -H "Content-Type: application/json" -d '{"motif":"vu sur place"}'
```

Statuts : `SIGNALE → CONFIRME → CLOS`, ou `SIGNALE → FAUSSE_ALERTE`.
Si `ms-foret` est indisponible, le signalement est conservé en `SECTEUR_A_VERIFIER` au lieu d'être perdu.

Erreurs normalisées :

```json
{"status": 409, "code": "TRANSITION_INVALIDE", "message": "Transition interdite : CLOS → CONFIRME"}
```

Codes métier : `SIGNALEMENT_INTROUVABLE`, `TRANSITION_INVALIDE`, `CONFLIT_CONCURRENCE`,
`DONNEES_INVALIDES`, `PARAMETRE_INVALIDE`, `CORPS_INVALIDE`, `ACCES_REFUSE`, `ERREUR_INTERNE`.

## 3. Événements

| Sens | Routing key | Contenu |
|---|---|---|
| Publie | `feu.confirme` | eventId, signalementId, secteurId, latitude, longitude, intensite, occurredAt |
| Consomme | `intervention.terminee` | eventId, incidentId, signalementId, secteurId, surfaceBruleeHa |

- File : `signalement.intervention-terminee` (durable), DLX `ffw.events.dlx`,
  DLQ `signalement.intervention-terminee.dlq`.
- **Outbox** : `feu.confirme` est d'abord écrit dans la collection `outbox` puis publié par un
  planificateur (toutes les 5 s) avec backoff exponentiel (5 s → 300 s), 10 tentatives maximum,
  puis statut `FAILED`. Un échec RabbitMQ ne fait donc plus échouer la confirmation.
- **Idempotence entrante** : chaque `eventId` est enregistré dans `processed_events` avant traitement.
  Un message redélivré est ignoré ; si le traitement échoue, le marqueur est retiré pour permettre le retry.
- **Retry/DLQ** : 3 tentatives en mémoire, puis rejet vers la DLQ (pas de boucle infinie).

Contrats à figer avec les autres équipes :

- `ms-foret` : `GET /api/secteurs/localiser?lat=&lng=` → `{ "id": 14, "code": "ZAG-03" }`.
- Coordination de crise : `InterventionTerminee` **doit** contenir `signalementId`.
  Repli accepté : si `signalementId` est absent, tous les `CONFIRME` du `secteurId` sont clôturés.
- Nom de l'exchange : le dossier S4 indique `reboistn.events`, ce service utilise `ffw.events`.
  À aligner avec le reste de l'équipe.

## 4. Robustesse et sécurité

- **Resilience4j** sur l'appel Forêt : circuit breaker (10 appels, seuil 50 %, 30 s d'ouverture),
  retry (3 essais) et timeout Feign (connexion 2 s, lecture 3 s). Repli : `Optional.empty()`.
- **Verrou optimiste** `@Version` : deux gardes qui confirment en parallèle ne peuvent pas publier
  deux `feu.confirme` ; le second reçoit `409 CONFLIT_CONCURRENCE`.
- **Index** : `2dsphere` sur `position`, `statut`, `creeLe`, composé `secteurId + statut`,
  unique sur `outbox.eventId`.
- **Validation** : coordonnées, intensité 1–5, description ≤ 500, URL de photo http(s) ≤ 2048,
  maximum 10 photos par signalement.
- **Keycloak** : rôles `realm_access.roles` convertis en `ROLE_*` ; JWT propagé aux appels Feign.
- **Actuator** : `/actuator/health`, `/actuator/info`, `/actuator/metrics`.

## 5. Tests

```bash
mvn test
```

- `SignalementServiceTest` (15 tests) : création, repli Forêt, doublons, transitions, photos, clôture.
- `SignalementControllerSecurityTest` (7 tests) : 401/403 par rôle, endpoints publics, paramètres invalides.
- `OutboxPublisherTest` (4 tests) : publication, retry/backoff, abandon, bornes du backoff.
- `EventDeduplicatorTest` (4 tests) : première exécution, doublon, échec, eventId absent.
- `SignalementRepositoryTest` (3 tests) : requête `$near` réelle, `@Version`, index.
  Ce test utilise **Testcontainers** : il est automatiquement ignoré si Docker n'est pas disponible
  (`disabledWithoutDocker = true`).

## 6. Configuration

`application.properties` contient les valeurs de développement. La configuration centralisée est dans
`Backend/config-repo/signalement.yml`, servie par le Config Server (profil `native`).

La documentation OpenAPI est générée automatiquement :
- Swagger UI : `http://localhost:8083/swagger-ui.html`
- JSON OpenAPI : `http://localhost:8083/v3/api-docs`
- Bouton **Authorize** : coller le JWT Keycloak (schéma `bearerAuth`) pour tester les endpoints protégés.

| Variable | Défaut | Rôle |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` | `dev` = sécurité ouverte, `prod` = JWT |
| `SERVER_PORT` | `8083` | Port HTTP |
| `CONFIG_SERVER_URL` | `http://localhost:8888` | Config Server (import optionnel) |
| `MONGO_URI` | `mongodb://localhost:27017/signalement_db` | MongoDB |
| `RABBIT_HOST` / `RABBIT_PORT` | `localhost` / `5672` | RabbitMQ |
| `RABBIT_USER` / `RABBIT_PASSWORD` | `guest` / `guest` | RabbitMQ |
| `EUREKA_URL` | `http://localhost:8761/eureka` | Eureka |
| `KEYCLOAK_URL` | `http://localhost:8180` | Keycloak |
| `OUTBOX_DELAY_MS` | `5000` | Fréquence du publieur outbox |

## 7. Intégration équipe

- **Gateway** (Thouraya) : ajouter la route
  `/api/signalements/** → lb://SIGNALEMENT` (le dossier impose un accès via la Gateway).
- **Config Server** : `Backend/config-server` (profil natif sur `Backend/config-repo`).
  `CONFIG_PROFILE=git` + `CONFIG_GIT_URI` pour lire le dépôt GitHub.
- **Eureka** : `Backend/eureka` (port 8761, `@EnableEurekaServer`).
- **Keycloak** : realm `forest-fire-watch` importé automatiquement par Compose.

> Le dossier technique S4 fourni décrit ReboisTN (reboisement) et non la surveillance des feux.
> Ce service appartient au domaine « forest fire watch » (`ffw.*`) : le dossier doit être mis à jour
> ou le périmètre confirmé avec l'équipe.

## 8. Limites connues

- L'outbox est « au moins une fois » : sans transaction MongoDB multi-documents, une panne entre
  l'écriture du signalement et celle de l'outbox peut perdre l'événement (fenêtre très courte).
  Une replica set MongoDB + `MongoTransactionManager` lèverait cette limite.
- Les photos sont des URLs : aucun upload de fichier ni stockage binaire.
- Le publieur outbox suppose une seule instance du service (pas de verrou distribué).
- L'application mobile et la Gateway ne font pas partie de ce dépôt.
