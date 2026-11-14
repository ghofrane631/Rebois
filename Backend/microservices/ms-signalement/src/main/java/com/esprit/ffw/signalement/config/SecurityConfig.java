package com.esprit.ffw.signalement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] OPEN = {"/actuator/health", "/actuator/info", "/v3/api-docs/**",
            "/swagger-ui/**", "/swagger-ui.html"};

    /** Profil dev : tout est ouvert (utile en S5, avant Keycloak). */
    @Bean
    @Profile("dev")
    SecurityFilterChain dev(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable()).authorizeHttpRequests(a -> a.anyRequest().permitAll());
        return http.build();
    }

    /** Profil normal : JWT Keycloak + rôles (S8). */
    @Bean
    @Profile("!dev")
    SecurityFilterChain secured(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .authorizeHttpRequests(a -> a
                .requestMatchers(OPEN).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/signalements/actifs").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/signalements").hasAnyRole("GARDE", "BENEVOLE")
                .requestMatchers(HttpMethod.PATCH, "/api/signalements/*/confirmer",
                        "/api/signalements/*/fausse-alerte").hasRole("GARDE")
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(keycloakConverter())));
        return http.build();
    }

    /** Keycloak met les rôles dans realm_access.roles → ROLE_GARDE, ROLE_BENEVOLE... */
    private JwtAuthenticationConverter keycloakConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realm = jwt.getClaim("realm_access");
            if (realm == null || realm.get("roles") == null) return List.of();
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) realm.get("roles");
            return roles.stream()
                    .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r))
                    .toList();
        });
        return converter;
    }
}
