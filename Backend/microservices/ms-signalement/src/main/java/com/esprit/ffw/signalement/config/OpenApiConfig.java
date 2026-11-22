package com.esprit.ffw.signalement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Forest Fire Watch — API Signalements",
                version = "1.0.0",
                description = """
                        Microservice **ms-signalement** : déclaration des départs de feu par les gardes
                        et bénévoles, localisation du secteur forestier, confirmation par un garde,
                        historique des statuts et publication de l'événement `feu.confirme`.

                        **Cycle de vie** : `SIGNALE` → `CONFIRME` → `CLOS`
                        (ou `SECTEUR_A_VERIFIER`, `FAUSSE_ALERTE`).

                        **Authentification** : JWT Keycloak du realm `forest-fire-watch`.
                        - `POST /api/signalements` : rôle `GARDE` ou `BENEVOLE`
                        - `PATCH .../confirmer` et `.../fausse-alerte` : rôle `GARDE`
                        - `GET /api/signalements/actifs` : public

                        En profil `dev`, la sécurité est ouverte et aucun jeton n'est nécessaire.
                        La documentation interactive est disponible sur `/swagger-ui.html`.
                        """,
                contact = @Contact(name = "Ghofrane Jridi — équipe ReboisTN")),
        servers = {
                @Server(url = "http://localhost:8083", description = "Service en direct"),
                @Server(url = "http://localhost:8080", description = "Via l'API Gateway (Eureka)")
        })
@SecurityScheme(
        name = OpenApiConfig.BEARER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Jeton d'accès Keycloak (realm forest-fire-watch). "
                + "Obtenir un token : POST /realms/forest-fire-watch/protocol/openid-connect/token")
public class OpenApiConfig {

    /** Nom du schéma de sécurité référencé par les contrôleurs. */
    public static final String BEARER = "bearerAuth";
}
