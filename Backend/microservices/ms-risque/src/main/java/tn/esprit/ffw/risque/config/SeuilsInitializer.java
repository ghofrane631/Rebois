package tn.esprit.ffw.risque.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.esprit.ffw.risque.entity.*;
import tn.esprit.ffw.risque.repository.SeuilRisqueRepository;

import java.util.List;

/** Seuils par défaut (modifiables ensuite via PUT /api/risque/seuils). */
@Configuration
public class SeuilsInitializer {
    @Bean
    ApplicationRunner initSeuils(SeuilRisqueRepository repo) {
        return args -> {
            if (repo.count() == 0) {
                repo.saveAll(List.of(
                        new SeuilRisque(NiveauRisque.FAIBLE, 0),
                        new SeuilRisque(NiveauRisque.MODERE, 25),
                        new SeuilRisque(NiveauRisque.ELEVE, 50),
                        new SeuilRisque(NiveauRisque.TRES_ELEVE, 70),
                        new SeuilRisque(NiveauRisque.EXTREME, 85)));
            }
        };
    }
}
