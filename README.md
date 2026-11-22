# Rebois - Service Discovery (Eureka Server & Clients)

**Architecture Web Distribuée (AWD)** · ESPRIT · 2026–2027  
**Projet** : Rebois / Forest Fire Watch  
**Membre(s) de l'équipe** : Ayoub Abed & équipe Rebois  

---

## 1. Description du Projet

Dans le cadre de l'architecture microservices distribuée de **Rebois**, un serveur de découverte **Netflix Eureka** a été mis en place pour permettre à l'ensemble des microservices (développés en différentes technologies : Java/Spring Boot, JavaScript/Node.js et Python/FastAPI) de s'enregistrer automatiquement et de se découvrir par leur nom logique (`spring.application.name` / VIP) plutôt que par des adresses IP écrites en dur.

---

## 2. Architecture & Microservices Enregistrés

| Microservice | Technologie | Port par défaut | Nom dans Eureka | Description |
|---|---|---|---|---|
| **Eureka Server** (`discovery-server`) | Spring Boot 3.3.5 / Spring Cloud | `8761` | — | Serveur de registre et Dashboard |
| **Candidat** | Spring Boot 3.2.5 + H2 | `8081` | `CANDIDAT` | API REST Candidats & Adresses |
| **Job** | Spring Boot 3.3.5 + MySQL | `8082` | `JOB` | API REST Offres d'emploi |
| **Candidature** | Spring Boot 3.3.5 + MySQL | `8085` | `CANDIDATURE` | API REST Candidatures |
| **Meeting** | Node.js / Express (`eureka-js-client`) | `8083` (ou `$PORT`) | `MEETING` | Microservice Réunions & Entretiens |
| **Notification** | Python / FastAPI (`py-eureka-client`) | `8084` (ou `$PORT`) | `NOTIFICATION` | Microservice Notifications |
| **Restauration** | Spring Boot 3.3.5 + MySQL + RabbitMQ | `8086` | `RESTAURATION-SERVICE` | Microservice Suivi Restauration forestière |

---

## 3. Détails d'Implémentation

### Partie 1 : Eureka Server (`Backend/discovery-server`)
- Annoté `@EnableEurekaServer`.
- Port par défaut : `8761`.
- Configuration désactivant l'auto-enregistrement :
  ```yaml
  server:
    port: 8761
  eureka:
    instance:
      hostname: localhost
    client:
      register-with-eureka: false
      fetch-registry: false
  ```
- Dashboard accessible sur [http://localhost:8761](http://localhost:8761).

### Partie 2 : Clients Spring Boot (`CANDIDAT`, `JOB`, `CANDIDATURE`, `RESTAURATION-SERVICE`)
- Dépendance : `spring-cloud-starter-netflix-eureka-client`.
- Annotation `@EnableDiscoveryClient` sur les classes principales.
- Configuration dans `application.properties` / `application.yml` :
  ```properties
  eureka.client.service-url.defaultZone=http://localhost:8761/eureka
  eureka.client.register-with-eureka=true
  eureka.client.fetch-registry=true
  eureka.instance.prefer-ip-address=true
  ```

### Partie 3 : Clients Polyglottes (Node.js & Python)
1. **Meeting (`Node.js / Express`)** :
   - Bibliothèque : `eureka-js-client`.
   - Lecture dynamique du port via la variable d'environnement `process.env.PORT` pour supporter le multi-instance.
   - Enregistrement sous `MEETING` au démarrage et arrêt propre via `client.stop()`.
   - Endpoint vérifié : `GET http://localhost:8083/api/meetings/hello`.

2. **Notification (`Python / FastAPI`)** :
   - Bibliothèque : `py-eureka-client`.
   - Gestionnaire de cycle de vie FastAPI (`lifespan`) pour s'enregistrer sous `NOTIFICATION` au démarrage (`init_async`) et se désinscrire à l'arrêt (`stop_async`).
   - Lecture dynamique du port via `os.getenv("PORT", "8084")`.
   - Endpoint vérifié : `GET http://localhost:8084/api/notifications/hello`.

---

## 4. Démarrage et Validation Multi-Instances

### Étape 1 : Démarrer le Discovery Server (Eureka)
```powershell
cd Backend/discovery-server
.\mvnw.cmd spring-boot:run
```

### Étape 2 : Démarrer les Microservices
```powershell
# Candidat (Port 8081)
cd Backend/microservices/candidat
.\mvnw.cmd spring-boot:run

# Meeting - Instance 1 (Port 8083)
cd Backend/microservices/meeting
$env:PORT="8083"; npm start

# Meeting - Instance 2 pour passage à l'échelle (Port 8093)
cd Backend/microservices/meeting
$env:PORT="8093"; npm start

# Notification (Port 8084)
cd Backend/microservices/notification
python -m app.main
```

### Étape 3 : Vérification du Dashboard Eureka
Accéder à **http://localhost:8761** :
- `CANDIDAT` apparaît avec l'état `UP`.
- `MEETING` apparaît avec 2 instances actives (`localhost:meeting:8083` et `localhost:meeting:8093`).
- `NOTIFICATION` apparaît avec l'état `UP`.
- Tous les endpoints `/hello` répondent avec un code HTTP 200.

---

## 5. Rendu CC (Checklist)

- [x] **Projet Eureka Server fonctionnel** (`discovery-server` sur le port 8761).
- [x] **Microservices Meeting (Node.js) et Notification (Python) configurés comme clients Eureka**.
- [x] **Services CANDIDAT, MEETING, NOTIFICATION enregistrés et visibles sur Eureka**.
- [x] **Deux instances de MEETING fonctionnant simultanément sur des ports différents** (`8083` et `8093`).
- [x] **Endpoints `GET /hello` fonctionnels** sur Meeting et Notification.
- [x] **Projet poussé sur GitHub** avec les noms des membres de l'équipe.
