# foret-service — ReboisTN

Microservice Spring Boot de gestion des **zones forestières tunisiennes**.

## Stack

| Technologie | Version |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.x |
| PostgreSQL + PostGIS | 15+ / 3.x |
| Spring Cloud (Eureka) | 2023.0.x |
| SpringDoc OpenAPI | 2.5.x |

## Prérequis

1. **PostgreSQL 15+** avec l'extension **PostGIS** installée.
2. Exécuter le script d'initialisation :
   ```bash
   psql -U postgres -f src/main/resources/init.sql
   ```
3. **Eureka Server** démarré sur `http://localhost:8761` (discovery-server du projet).

## Lancement

### Via Maven
```bash
export DB_USER=postgres
export DB_PASSWORD=postgres
./mvnw spring-boot:run
```

### Via Docker
```bash
# 1. Builder le jar
./mvnw clean package -DskipTests

# 2. Builder l'image
docker build -t foret-service:latest .

# 3. Démarrer
docker run -p 8081:8081 \
  -e DB_USER=postgres \
  -e DB_PASSWORD=postgres \
  -e EUREKA_URI=http://host.docker.internal:8761/eureka/ \
  foret-service:latest
```

## Endpoints principaux

| Méthode | URL | Description |
|---|---|---|
| `POST` | `/api/zones` | Créer une zone (→ 201 + Location) |
| `GET` | `/api/zones` | Lister les zones (paginé, filtrable) |
| `GET` | `/api/zones/{id}` | Détail d'une zone |
| `PUT` | `/api/zones/{id}` | Mettre à jour une zone |
| `DELETE` | `/api/zones/{id}` | Archiver logiquement une zone (→ 204) |
| `PATCH` | `/api/zones/{id}/archive` | Archiver explicitement (→ 200) |
| `GET` | `/api/regions` | Lister les régions |
| `POST` | `/api/regions` | Créer une région |
| `GET` | `/api/regions/{id}/delegations` | Délégations d'une région |
| `POST` | `/api/delegations` | Créer une délégation |

### Paramètres de filtrage `GET /api/zones`

| Paramètre | Type | Défaut | Description |
|---|---|---|---|
| `page` | int | 0 | Numéro de page |
| `size` | int | 10 | Taille de la page |
| `type` | enum | — | `FORET`, `RESERVE`, `PARC`, `REBOISEMENT`, `PRIVE` |
| `statut` | enum | `ACTIVE` | `ACTIVE` ou `ARCHIVEE` |
| `delegationId` | Long | — | Filtrer par délégation |
| `regionId` | Long | — | Filtrer par région |

## Swagger UI

> <http://localhost:8081/swagger-ui.html>

## Enregistrement Eureka

Le service s'enregistre automatiquement sous le nom **`foret-service`** auprès du serveur Eureka (port 8761).

## Notes

- `DELETE /api/zones/{id}` effectue un **archivage logique** (statut → `ARCHIVEE`), aucune suppression physique.
- La géométrie est optionnelle et acceptée en **WKT Polygon** (ex : `POLYGON((9.5 36.8, ...))`).
- Les zones archivées sont **exclues par défaut** des listes (passer `?statut=ARCHIVEE` pour les inclure).
