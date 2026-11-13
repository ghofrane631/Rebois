# ressources-service : Service 5 de Forest Fire Watch

Recense les moyens de lutte (camions-citernes, équipes au sol, hélicoptères, bulldozers) de chaque caserne
et gère leur **disponibilité**, leur **réservation**, leur **pré-positionnement** et leur **libération**.

| | |
|---|---|
| Technologie | Spring Boot 3.3 · Java 17 · Maven |
| Base | MySQL : `ressources_db` (propre au service, aucune table partagée) |
| Port | 8085 |
| Eureka | `ressources-service` |
| Swagger | http://localhost:8085/swagger-ui.html |

## Règles métier

1. **Les plus proches d'abord** : les moyens sont triés par distance GPS au feu (formule de Haversine).
2. **Réserve minimale** : une caserne garde toujours au moins 1 camion-citerne disponible (`ressources.reserve-minimale-camions`).
3. **Tout ou rien** : si on ne trouve pas assez de moyens, rien n'est réservé, `MoyensInsuffisants` est publié, et la réponse est 409.
4. **Verrouillage optimiste** (`@Version`) : si deux incidents prennent le même moyen en même temps, le perdant recommence avec les moyens libres restants (3 essais au maximum).
5. Un moyen `RESERVEE` ne peut pas partir en maintenance (409).

## Contrat d'API

| Méthode | Route | Description | Codes |
|---|---|---|---|
| POST | `/api/ressources` | Recenser un moyen | 201, 400 |
| GET | `/api/ressources?gouvernorat=&statut=` | Moyens par zone et statut | 200 |
| GET | `/api/ressources/{id}` | Détail d'un moyen | 200, 404 |
| PATCH | `/api/ressources/{id}/statut` | Maintenance ou retour | 200, 404, 409 |
| POST | `/api/ressources/reservations` | Réserver les plus proches (appelé par Coordination de crise) | 201, 400, 409 |
| GET | `/api/ressources/reservations?incidentId=` | Moyens d'un incident | 200 |
| DELETE | `/api/ressources/reservations/{id}` | Libérer une réservation | 204, 404 |

```http
POST /api/ressources/reservations
{"incidentId":5, "type":"CAMION_CITERNE", "nombre":2, "lat":36.42, "lng":10.08}
→ 201 {"incidentId":5, "ressources":[1, 2], "reservations":[...]}
→ 409 {"error":"AUCUN_MOYEN_DISPONIBLE", "message":"Demandés : 2 CAMION_CITERNE, disponibles : 1"}
```

## Communications

**Synchrone (OpenFeign)**
- **Appelé par** Coordination de crise, pour réserver des moyens.
- N'appelle aucun service : en urgence, la réservation répond sans dépendre de personne.
- **Si Ressources est en panne**, Coordination de crise passe l'incident en `MOYENS_A_AFFECTER`.

**Asynchrone (RabbitMQ, exchange `forestfire.events`)**

| Sens | Événement | Effet |
|---|---|---|
| Consommé | `NiveauRisqueChange` (Risque) | En EXTRÊME : pré-positionne le camion libre le plus proche |
| Consommé | `InterventionTerminee` (Coordination de crise) | Libère tous les moyens de l'incident |
| Publié | `MoyensInsuffisants` → Coordination de crise | Aucun moyen disponible (réservation ou pré-positionnement) |

**Fiabilité :**
- **Outbox** : l'événement est écrit dans la même transaction que la donnée, puis publié toutes les 2 s. Si RabbitMQ est en panne, il reste dans l'outbox.
- **Idempotence** : chaque eventId traité est stocké dans `evenement_traite`, et un doublon est ignoré.
- **Retry** : 3 essais, puis le message part dans la dead-letter queue `ressources.dlq`.
- **Au redémarrage** du service, les événements qui attendaient dans RabbitMQ sont traités.

## Lancer le service dans IntelliJ

La configuration est centralisée : `application.yml` ne contient que le nom du service et l'adresse du
config-server. Tout le reste (port, MySQL, RabbitMQ, Eureka, règles métier) est dans `config-repo/ressources-service.yml`.

**Ordre de démarrage :**
1. MySQL (XAMPP : `root` sans mot de passe). La base `ressources_db` est créée automatiquement.
2. `config-server` (port 8888).
3. `discovery-server` (Eureka, port 8761).
4. `ressources-service` : lancer `RessourcesServiceApplication`. 10 moyens de démonstration sont insérés au premier démarrage.
5. `api-gateway`.

**Vérifier :**
- http://localhost:8888/ressources-service/default affiche la configuration lue dans config-repo ;
- http://localhost:8761 montre `RESSOURCES-SERVICE` en UP ;
- http://localhost:8085/swagger-ui.html affiche l'API.

Si RabbitMQ n'est pas lancé, le service démarre quand même. Les erreurs de connexion s'affichent dans les logs, et les événements attendent dans l'outbox.

**Tests** : `mvn test`, ou un clic droit sur `src/test/java` puis **Run 'All Tests'**. Ils utilisent H2 en mémoire
et n'ont besoin ni de MySQL ni de RabbitMQ.

## Démo pour la validation (avec les données de démo)

1. `POST /api/ressources/reservations` avec `{"incidentId":5,"type":"CAMION_CITERNE","nombre":2,"lat":36.42,"lng":10.08}`
   → **201** : 2 camions de Zaghouan, la caserne la plus proche.
2. Le même appel avec `"incidentId":6` → **409** `AUCUN_MOYEN_DISPONIBLE`. Il reste 1 camion réservable à Jendouba,
   car Zaghouan et Siliana gardent leur dernier camion. `MoyensInsuffisants` part dans l'outbox.
3. `GET /api/ressources/reservations?incidentId=5` → les 2 réservations `ACTIVE`.
4. `DELETE /api/ressources/reservations/{id}` → **204**, et le camion redevient `DISPONIBLE`.
