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
| Swagger | http://localhost:8085/swagger-ui.html |
| Santé | http://localhost:8085/actuator/health |

## Lancement

Prérequis : Java 17, Maven, MySQL sur le port 3306 (Eureka sur 8761 est optionnel en local).

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

Tests : `mvn test` (base H2 en mémoire, utilisée uniquement pour les tests).

## Modèle

- `Incident` : signalementId (unique, un incident par feu), secteurId, gouvernorat, niveauRisque, ouvertLe, statut
- `MoyenEngage`, `EvenementChrono`, `Bilan` : rattachés à un incident
- `AlerteEmise`, `Escalade`, `RegleEscalade`, `Abonnement` : alertes de la zone

Statuts : `OUVERT` → `EN_COURS` (ou `MOYENS_A_AFFECTER`) → `MAITRISE` → `CLOS`.

## API

| Méthode | Route | Description | Codes |
|---|---|---|---|
| GET | `/api/incidents?statut=` | Lister les incidents | 200 |
| POST | `/api/incidents` | Ouvrir un incident (alerte les abonnés de la zone) | 201, 400, 409 |
| GET | `/api/incidents/{id}` | Détail : chronologie, moyens, alertes, bilan | 200, 404 |
| POST | `/api/incidents/{id}/moyens` | Engager un moyen | 201, 400, 404, 409 |
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
- la clôture (`CLOS`) ne passe que par le bilan, et seulement quand le feu est `MAITRISE` (409 sinon) ;
- un incident clos ne peut plus être modifié (409) ;
- en risque `EXTREME`, l'alerte part par SMS (envoi simulé dans les logs pour l'instant).

Exemple :

```http
POST /api/incidents
{"signalementId": 88, "secteurId": 14, "gouvernorat": "Zaghouan", "niveauRisque": "EXTREME"}
→ 201 {"id": 1, "statut": "OUVERT", ...}
```

## Prochaines étapes

- S6 : appels OpenFeign vers ms-signalement, ms-ressources et ms-user, avec replis
- S7 : consommer `FeuConfirme`, `NiveauRisqueChange`, `MoyensInsuffisants`, `UtilisateurCree` ; publier `InterventionTerminee`
- S7 : escalade automatique des alertes non lues (`@Scheduled`)
- S8 : identité et rôles lus dans le jeton Keycloak
