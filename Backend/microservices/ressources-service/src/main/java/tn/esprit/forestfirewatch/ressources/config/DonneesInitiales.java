package tn.esprit.forestfirewatch.ressources.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import tn.esprit.forestfirewatch.ressources.entity.Ressource;
import tn.esprit.forestfirewatch.ressources.repository.RessourceRepository;

import java.util.List;

import static tn.esprit.forestfirewatch.ressources.entity.TypeRessource.*;

/** Jeu de données de démonstration, inséré seulement si la table est vide. */
@Component
@ConditionalOnProperty(name = "ressources.donnees-demo", havingValue = "true", matchIfMissing = true)
public class DonneesInitiales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DonneesInitiales.class);

    private final RessourceRepository ressourceRepository;

    public DonneesInitiales(RessourceRepository ressourceRepository) {
        this.ressourceRepository = ressourceRepository;
    }

    @Override
    public void run(String... args) {
        if (ressourceRepository.count() > 0) {
            return;
        }
        ressourceRepository.saveAll(List.of(
                // Caserne 1 : Zaghouan
                new Ressource(CAMION_CITERNE, 1L, "Zaghouan", 36.4029, 10.1429),
                new Ressource(CAMION_CITERNE, 1L, "Zaghouan", 36.4029, 10.1429),
                new Ressource(CAMION_CITERNE, 1L, "Zaghouan", 36.4029, 10.1429),
                new Ressource(EQUIPE_AU_SOL, 1L, "Zaghouan", 36.4029, 10.1429),
                // Caserne 2 : Jendouba
                new Ressource(CAMION_CITERNE, 2L, "Jendouba", 36.5011, 8.7802),
                new Ressource(CAMION_CITERNE, 2L, "Jendouba", 36.5011, 8.7802),
                new Ressource(HELICOPTERE, 2L, "Jendouba", 36.5011, 8.7802),
                new Ressource(EQUIPE_AU_SOL, 2L, "Jendouba", 36.5011, 8.7802),
                // Caserne 3 : Siliana
                new Ressource(CAMION_CITERNE, 3L, "Siliana", 36.0849, 9.3708),
                new Ressource(BULLDOZER, 3L, "Siliana", 36.0849, 9.3708)
        ));
        log.info("Données de démonstration insérées : 10 moyens dans 3 casernes");
    }
}
