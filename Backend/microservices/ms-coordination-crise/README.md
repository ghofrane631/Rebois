# ms-coordination-crise

Microservice **Coordination de crise** du projet Forest Fire Watch.
Responsable : Mezen Elghoul.

Il gère chaque incident de bout en bout : un feu confirmé ouvre un incident, qui porte la lutte
(moyens engagés, chronologie, bilan) et les alertes de la zone (abonnements, escalade).

| Élément | Valeur |
|---|---|
| Port | 8088 (2e instance : `SERVER_PORT=8098`) |
| Nom Eureka | `coordination-crise` (affiché `COORDINATION-CRISE`) |
| Base | MySQL `coordination_db` (créée au démarrage si absente) |
| Messagerie | RabbitMQ, exchange `ffw.events` |
| Swagger | http://localhost:8088/swagger-ui.html |
| Santé | http://localhost:8088/actuator/health |

## Lancement

Prérequis : Java 17, Maven, MySQL sur le port 3306. Eureka (8761) et RabbitMQ (5672) sont
optionnels en local : sans eux, le service démarre et applique ses comportements de repli.

```bash
# le mot de passe MySQL n'est jamais écrit dans le dépôt
export DB_PASSWORD=...        # PowerShell : $env:DB_PASSWORD="..."
mvn spring-boot:run
```

| Variable | Défaut | Rôle |
|---|---|---|
| `SERVER_PORT` | `8088` | port de l'instance |
| `DB_HOST` | `localhost` | hôte MySQL |
| `DB_USER` | `root` | utilisateur MySQL |
| `DB_PASSWORD` | vide | mot de passe MySQL |
| `EUREKA_URL` | `http://localhost:8761/eureka` | annuaire Eureka |
| `CONFIG_SERVER_URL` | `http://localhost:8888` | Config Server de l'équipe (optionnel) |
| `RABBIT_HOST` | `localhost` | serveur RabbitMQ |
| `EVENTS_EXCHANGE` | `ffw.events` | exchange commun des événements |
| `RABBIT_HEALTH` | `false` | inclure RabbitMQ dans `/actuator/health` (à activer quand RabbitMQ tourne) |
| `RESSOURCES_SERVICE` / `SIGNALEMENT_SERVICE` / `USER_SERVICE` | `ressources-service` / `signalement` / `ms-user` | noms Eureka des services appelés |

Docker (après `mvn package`) :

```bash
docker build -t ms-coordination-crise .
docker run -p 8088:8088 -e DB_HOST=host.docker.internal -e DB_PASSWORD=... -e EUREKA_URL=http://host.docker.internal:8761/eureka ms-coordination-crise
```

Tests : `mvn test` (profil `test` : base H2 en mémoire, utilisée uniquement pour les tests).

## Modèle

- `Incident` : signalementId (unique, un incident par feu), secteurId, position (latitude, longitude), gouvernorat, niveauRisque, ouvertLe, statut
- `MoyenEngage`, `EvenementChrono`, `Bilan` : rattachés à un incident
- `AlerteEmise`, `Escalade`, `RegleEscalade`, `Abonnement` : alertes de la zone

Statuts : `OUVERT` → `EN_COURS` (ou `MOYENS_A_AFFECTER`) → `MAITRISE` → `CLOS`.

## API

| Méthode | Route | Description | Codes |
|---|---|---|---|
| GET | `/api/incidents?statut=` | Lister les incidents | 200 |
| POST | `/api/incidents` | Ouvrir un incident : alerte la zone et réserve 2 camions | 201, 400, 409 |
| GET | `/api/incidents/{id}` | Détail : chronologie, moyens, alertes, bilan | 200, 404 |
| POST | `/api/incidents/{id}/moyens` | Réserver des moyens auprès de Ressources | 201, 400, 404, 409, 503 |
| POST | `/api/incidents/{id}/chronologie` | Ajouter une action | 201, 400, 404, 409 |
| PATCH | `/api/incidents/{id}/statut` | Changer le statut (sauf CLOS) | 200, 400, 404, 409 |
| PUT | `/api/incidents/{id}/bilan` | Enregistrer le bilan et clore | 200, 400, 404, 409 |
| GET | `/api/incidents/{id}/alertes` | Alertes d'un incident | 200, 404 |
| GET | `/api/alertes/me?lue=` | Mes alertes (en-tête `X-User-Id`) | 200, 400 |
| PATCH | `/api/alertes/{id}/lue` | Accuser réception | 200, 404, 409 |
| GET | `/api/alertes/{id}/escalades` | Escalades d'une alerte | 200, 404 |
| GET | `/api/abonnements?userId=` | Lister les abonnements | 200 |
| POST | `/api/abonnements` | S'abonner à une zone | 201, 400, 409 |
| DELETE | `/api/abonnements/{id}` | Se désabonner | 204, 404 |
| GET | `/api/regles-escalade` | Lister les règles d'escalade | 200 |
| POST | `/api/regles-escalade` | Créer une règle | 201, 400, 409 |
| PUT | `/api/regles-escalade/{id}` | Modifier une règle | 200, 400, 404 |

Règles principales :
- un seul incident par signalement (409 sinon) ;
- transitions permises : `OUVERT` → `EN_COURS` ou `MOYENS_A_AFFECTER` → `EN_COURS` → `MAITRISE` (→ `EN_COURS` si le feu reprend) ;
- la clôture (`CLOS`) ne passe que par le bilan, et seulement quand le feu est `MAITRISE` (409 sinon) ;
- un incident clos ne peut plus être modifié (409) ;
- en risque `EXTREME`, l'alerte part par SMS (envoi simulé dans les logs) ;
- une alerte non lue après le délai de sa règle (10 min pour un incident) est escaladée une seule fois.

Exemple :

```http
POST /api/incidents
{"signalementId": 88, "secteurId": 14, "gouvernorat": "Zaghouan", "niveauRisque": "EXTREME",
 "latitude": 36.42, "longitude": 10.08}
→ 201 {"id": 1, "statut": "EN_COURS", ...}
```

## Communication

Synchrone (OpenFeign) : quand la réponse est nécessaire pour continuer.

| Service appelé | Donnée | Si le service est indisponible |
|---|---|---|
| ressources-service | réservation des moyens les plus proches | incident en `MOYENS_A_AFFECTER`, réservation à relancer (503) |
| signalement | position du feu si elle manque | incident ouvert sans position, en `MOYENS_A_AFFECTER` |
| ms-user | utilisateurs de la zone à alerter | seuls les abonnés locaux sont alertés |

Asynchrone (RabbitMQ, exchange `ffw.events`) : pour informer sans attendre de réponse.

| Événement | Clé de routage | Sens | Effet |
|---|---|---|---|
| `FeuConfirme` | `feu.confirme` | consommé | ouvre un incident |
| `NiveauRisqueChange` | `risque.niveau.change` | consommé | alerte de vigilance à partir de `ELEVE` |
| `MoyensInsuffisants` | `moyens.insuffisants` | consommé | alerte régionale par SMS |
| `UtilisateurCree` | `utilisateur.cree` | consommé | abonnement par défaut à la zone |
| `InterventionTerminee` | `intervention.terminee` | publié | à la clôture, vers Forêt, Signalement, Risque, Ressources, Restauration |

Chaque événement consommé est traité une seule fois (eventId enregistré). Un message en échec après
3 essais part dans la file `coordination.dlq`.

## Observabilité (Eureka et Actuator)

Le service s'enregistre dans Eureka (`discovery-server`, port 8761) sous le nom `coordination-crise`,
avec un identifiant par instance (`coordination-crise:8088`, `coordination-crise:8098`…).

| Endpoint | Contenu |
|---|---|
| `/actuator/health/liveness`, `/actuator/health/readiness` | le service est-il vivant ? prêt à recevoir du trafic ? |
| `/actuator/health` | statut global et détail des composants : `db` (MySQL), `diskSpace`, `ping`, `discoveryComposite`, `ressources` |
| `/actuator/info` | nom, version, description, équipe (le lien de l'instance dans le dashboard Eureka y mène) |
| `/actuator/metrics` | métriques JVM et HTTP, ex. `/actuator/metrics/http.server.requests` |
| `/actuator/loggers` | niveau des logs, modifiable sans redémarrer |

- `eureka.client.healthcheck.enabled=true` : Eureka reçoit le vrai statut de `/actuator/health`.
  Si MySQL s'arrête, le composant `db` passe DOWN, l'instance passe **DOWN** dans le dashboard
  et n'est plus proposée aux autres services ; elle repasse UP quand MySQL redémarre.
- `RessourcesHealthIndicator` (HealthIndicator personnalisé, composant `ressources`) : indique si
  `ressources-service` est disponible dans Eureka et combien d'instances. Le statut reste UP sans
  Ressources, car le service continue de fonctionner (incidents en `MOYENS_A_AFFECTER`).

Lancer deux instances :

```powershell
mvn spring-boot:run                                              # instance 1 : port 8088
mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8098"   # instance 2
```

## Prochaines étapes

- S7 : table outbox pour republier `InterventionTerminee` si RabbitMQ est indisponible
- S8 : identité et rôles lus dans le jeton Keycloak (aujourd'hui en-tête `X-User-Id`)
