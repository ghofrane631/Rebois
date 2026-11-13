# ms-coordination-crise

Microservice **Coordination de crise** du projet Forest Fire Watch.
Responsable : Mezen Elghoul.

Il gère chaque incident de bout en bout : un feu confirmé ouvre un incident, qui porte la lutte
(moyens engagés, chronologie, bilan) et les alertes de la zone (abonnements, escalade).

| Élément | Valeur |
|---|---|
| Port | 8085 |
| Nom Eureka | `COORDINATION-CRISE` |
| Base | MySQL `coordination_db` (créée au démarrage si absente) |
| Messagerie | RabbitMQ, exchange `ffw.events` |
| Swagger | http://localhost:8085/swagger-ui.html |
| Santé | http://localhost:8085/actuator/health |

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
| `DB_HOST` | `localhost` | hôte MySQL |
| `DB_USER` | `root` | utilisateur MySQL |
| `DB_PASSWORD` | vide | mot de passe MySQL |
| `EUREKA_URL` | `http://localhost:8761/eureka` | annuaire Eureka |
| `RABBIT_HOST` | `localhost` | serveur RabbitMQ |
| `EVENTS_EXCHANGE` | `ffw.events` | exchange commun des événements |
| `RESSOURCES_SERVICE` / `SIGNALEMENT_SERVICE` / `USER_SERVICE` | `ressources-service` / `ms-signalement` / `ms-user` | noms Eureka des services appelés |

Tests : `mvn test` (base H2 en mémoire, utilisée uniquement pour les tests).

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
| ms-ressources | réservation des moyens les plus proches | incident en `MOYENS_A_AFFECTER`, réservation à relancer (503) |
| ms-signalement | position du feu si elle manque | incident ouvert sans position, en `MOYENS_A_AFFECTER` |
| ms-user | utilisateurs de la zone à alerter | seuls les abonnés locaux sont alertés |

Asynchrone (RabbitMQ, exchange `ffw.events`) : pour informer sans attendre de réponse.

| Événement | Sens | Effet |
|---|---|---|
| `FeuConfirme` | consommé | ouvre un incident |
| `NiveauRisqueChange` | consommé | alerte de vigilance à partir de `ELEVE` |
| `MoyensInsuffisants` | consommé | alerte régionale par SMS |
| `UtilisateurCree` | consommé | abonnement par défaut à la zone |
| `InterventionTerminee` | publié | à la clôture, vers Forêt, Signalement, Risque, Ressources, Restauration |

Chaque événement consommé est traité une seule fois (eventId enregistré). Un message en échec après
3 essais part dans la file `coordination.dlq`.

## Prochaines étapes

- S7 : table outbox pour republier `InterventionTerminee` si RabbitMQ est indisponible
- S8 : identité et rôles lus dans le jeton Keycloak (aujourd'hui en-tête `X-User-Id`)
