package tn.esprit.forestfirewatch.ressources;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Service 5 de Forest Fire Watch : Ressources.
 * Recense les moyens de lutte de chaque caserne et gère leur disponibilité,
 * leur réservation, leur pré-positionnement et leur libération.
 */
@SpringBootApplication
@EnableScheduling
public class RessourcesServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RessourcesServiceApplication.class, args);
    }
}
