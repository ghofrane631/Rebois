# Restauration Service

Microservice Spring Boot chargé du suivi des zones forestières après une intervention.
Le cycle métier est : `A_EVALUER` → `EVALUEE` → `EN_RESTAURATION` → `RESTAUREE`.

## Démarrage

Prérequis : Java 17+, Maven, MySQL et RabbitMQ.

Depuis ce dossier, définir `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `RABBITMQ_HOST`,
`RABBITMQ_PORT`, `RABBITMQ_USERNAME`, `RABBITMQ_PASSWORD` et `FORET_SERVICE_URL`,
puis lancer :

```powershell
mvn spring-boot:run
```

Le port par défaut est `8086`. La base MySQL est créée automatiquement avec les valeurs
de développement (utilisateur `root`, sans mot de passe pour XAMPP); en déploiement,
fournir les identifiants par variables d'environnement.
La spécification OpenAPI est accessible sur `/v3/api-docs` et Swagger UI sur `/swagger-ui/index.html`.

## API

Toutes les routes sont préfixées par `/api/restauration`.

| Méthode | Route | Description |
| --- | --- | --- |
| GET | `/zones?statut=&gouvernorat=` | Lister/filtrer les zones |
| GET | `/zones/{id}` | Détail d'une zone |
| PATCH | `/zones/{id}/statut` | Changer le statut sans sauter d'étape |
| POST | `/zones/{id}/evaluation` | Enregistrer l'évaluation des dégâts |
| POST | `/zones/{id}/plan` | Créer le plan après évaluation |
| PATCH | `/actions/{id}` | Marquer une action réalisée |
| POST | `/zones/{id}/suivis` | Ajouter un suivi de repousse |

Exemple de plan :

```json
{
  "budgetEstime": 120000,
  "actions": [
    { "type": "PROTECTION_SOL", "description": "Installer des fascines" },
    { "type": "REBOISEMENT", "description": "Planter des pins d'Alep" }
  ]
}
```

Un plan sans évaluation retourne `409 EVALUATION_REQUISE`. Une couverture de repousse
supérieure ou égale à 70 % fait passer la zone à `RESTAUREE` et déclenche l'événement
`ZoneRestauree`.

## Événements

Le service consomme `InterventionTerminee` avec les champs `eventId`, `incidentId`,
`secteurId`, `surfaceBruleeHa` et, facultativement, `gouvernorat`. `eventId` est mémorisé
pour ignorer les doublons; l'identifiant d'incident est également unique.

Le service appelle Forêt via `GET /api/secteurs/{secteurId}/vegetation` pour récupérer
la végétation cible. Si Forêt est indisponible, le plan est créé sans végétation cible.

Les échanges RabbitMQ sont configurables dans `application.yml` via les clés
`app.events.*`. Le contrôle JWT et des rôles doit être appliqué par l'API Gateway du projet.
Les événements sortants sont sauvegardés dans l'outbox MySQL avec les changements métier,
puis marqués publiés uniquement après confirmation du routage par RabbitMQ.

## Tests

`mvn test` utilise MySQL avec une base dédiée `restauration_test` et recrée son schéma.
Configurer `TEST_DB_URL`, `TEST_DB_USERNAME` et `TEST_DB_PASSWORD` si les valeurs locales
par défaut ne correspondent pas. Les tests HTTP démarrent un serveur Spring sur un port
aléatoire; RabbitMQ n'est pas requis pour eux.