package tn.reboistn.coordination.health;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Composant "ressources" de /actuator/health : ms-ressources est-il disponible dans Eureka ?
 *
 * Sans Ressources, ce service continue de fonctionner (l'incident passe en MOYENS_A_AFFECTER
 * et la réservation est relancée plus tard). Le statut reste donc UP, avec le détail
 * "reservations en attente", pour ne pas retirer l'instance d'Eureka à tort.
 */
@Component("ressources")
@RequiredArgsConstructor
public class RessourcesHealthIndicator implements HealthIndicator {

    private final DiscoveryClient discoveryClient;

    @Value("${clients.ressources:ressources-service}")
    private String serviceRessources;

    @Override
    public Health health() {
        try {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceRessources);
            if (instances.isEmpty()) {
                return Health.up()
                        .withDetail("service", serviceRessources)
                        .withDetail("instances", 0)
                        .withDetail("impact", "reservations en attente : incidents en MOYENS_A_AFFECTER")
                        .build();
            }
            return Health.up()
                    .withDetail("service", serviceRessources)
                    .withDetail("instances", instances.size())
                    .withDetail("adresses", instances.stream().map(i -> i.getHost() + ":" + i.getPort()).toList())
                    .build();
        } catch (RuntimeException e) {
            return Health.unknown()
                    .withDetail("service", serviceRessources)
                    .withDetail("erreur", "annuaire Eureka injoignable")
                    .build();
        }
    }
}
