package tn.esprit.ffw.risque.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain chain(HttpSecurity http) throws Exception {
        http.csrf(c -> c.disable())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(a -> a
                .requestMatchers("/actuator/health/**").permitAll()
                .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/risque/secteurs/*", "/api/risque/carte").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/risque/releves").hasRole("GARDE")
                .requestMatchers(HttpMethod.PUT, "/api/risque/seuils").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(o -> o.jwt(j -> j.jwtAuthenticationConverter(keycloakConverter())));
        return http.build();
    }

    /** Rôles Keycloak (realm_access.roles) -> ROLE_GARDE, ROLE_ADMIN... */
    private JwtAuthenticationConverter keycloakConverter() {
        JwtAuthenticationConverter c = new JwtAuthenticationConverter();
        c.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            Collection<GrantedAuthority> out = new ArrayList<>();
            Object realm = jwt.getClaims().get("realm_access");
            if (realm instanceof Map<?, ?> m && m.get("roles") instanceof Collection<?> roles) {
                roles.forEach(r -> out.add(new SimpleGrantedAuthority("ROLE_" + r)));
            }
            return out;
        });
        c.setPrincipalClaimName("preferred_username");
        return c;
    }
}
