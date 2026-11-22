# ms-risque — Forest Fire Watch

Service **Risque** : calcule un indice de risque d'incendie (0–100) par secteur et publie les changements de niveau.

- Port : `8083` · Base MySQL : `risque_db` · Eureka : `ms-risque`
- Java 17 / Spring Boot 3.3 / Spring Cloud 2023.0

## Formule
```
scoreMeteo = 0.35·Temp + 0.25·(100 − Humidité) + 0.20·Vent + 0.20·JoursSansPluie
   Temp : 10→45 °C = 0→100 | Vent : 0→60 km/h = 0→100 | Jours sans pluie : 0→30 = 0→100
indice     = clamp(scoreMeteo + 5 × feuxActifs(30 j) + ajustement, 0, 100)
ajustement = −10 par ZoneRestauree (plancher −30)
```
Seuils par défaut (modifiables par ADMIN) : FAIBLE 0 · MODERE 25 · ELEVE 50 · TRES_ELEVE 70 · EXTREME 85.

## API (via Gateway)
| Méthode | Route | Accès | Codes |
|---|---|---|---|
| POST | /api/risque/releves | GARDE | 201, 400, 404, 503 |
| GET | /api/risque/secteurs/{id} | Public | 200, 404 |
| GET | /api/risque/carte?niveau= | Public | 200 |
| GET | /api/risque/releves?secteurId=&from=&to= | Authentifié | 200 |
| GET | /api/risque/secteurs/{id}/historique | Authentifié | 200 |
| PUT | /api/risque/seuils | ADMIN | 200, 400 |

## Swagger UI
Après avoir démarré le service, l'interface interactive est disponible sur `http://localhost:8083/swagger-ui.html`.
La spécification OpenAPI JSON est disponible sur `http://localhost:8083/v3/api-docs`.
Les routes protégées nécessitent un jeton JWT valide ; Swagger UI ne remplace pas l'authentification Keycloak.

## Communication

### Synchrone (OpenFeign + Resilience4j)
`POST /releves` → `GET ms-foret /api/foret/secteurs/{id}`
- 404 de Forêt → **404** chez nous (secteur inconnu)
- Forêt en panne / circuit ouvert → **503** (fallbackFactory), le relevé n'est pas enregistré
- L'appel est fait **avant** la transaction MySQL.

### Asynchrone (RabbitMQ, exchange topic `ffw.events`)
| Sens | Événement | Routing key | Effet |
|---|---|---|---|
| ← consomme | SecteurCree | secteur.cree | indice initial FAIBLE |
| ← consomme | FeuConfirme | feu.confirme | +5 par feu actif, recalcul |
| ← consomme | InterventionTerminee | intervention.terminee | feu marqué éteint, recalcul |
| ← consomme | ZoneRestauree | zone.restauree | ajustement −10, recalcul |
| → publie | NiveauRisqueChange | risque.niveau.change | Ressources + Coordination de crise |

- **Idempotence** : table `evenement_traite` (clé `eventId`), vérifiée dans la même transaction que le traitement.
- **Publication après commit** (`@TransactionalEventListener AFTER_COMMIT`) : jamais d'événement pour une écriture annulée.
- **Retry** 3 tentatives puis **dead-letter** (`risque.dlq`).
- **Verrou optimiste** (`@Version`) sur `indice_secteur` contre les événements concurrents.

Payloads JSON consommés (champ `eventId` obligatoire, UUID) :
```json
{"eventId":"7b9c...","feuId":12,"secteurId":3,"dateConfirmation":"2026-11-13T10:30:00"}
{"eventId":"a1f0...","secteurId":3}
```
Payload publié :
```json
{"eventId":"...","secteurId":3,"ancienNiveau":"ELEVE","nouveauNiveau":"EXTREME",
 "indice":90,"cause":"RELEVE_METEO","date":"2026-11-13T10:30:05"}
```

## Lancer
Prérequis : MySQL, RabbitMQ, Eureka (8761), Keycloak (realm `forestfirewatch`, rôles `GARDE`, `ADMIN`), ms-foret.
```bash
mvn test
mvn spring-boot:run
```

## Démo
```bash
curl -X POST http://localhost:8080/api/risque/releves \
  -H "Authorization: Bearer $TOKEN_GARDE" -H "Content-Type: application/json" \
  -d '{"secteurId":3,"temperature":41,"humidite":12,"vent":50,"joursSansPluie":30}'
# -> 201, indice ≈ 90, niveau EXTREME, événement NiveauRisqueChange publié
```
Test sans Forêt (RabbitMQ Management → exchange `ffw.events` → Publish message, routing key `feu.confirme`).

## Hypothèses à confirmer avec l'équipe
- Route Forêt : `/api/foret/secteurs/{id}` et nom Eureka `ms-foret`
- Noms d'exchange / routing keys partagés avec les autres services
- Les événements entrants portent un `eventId`
