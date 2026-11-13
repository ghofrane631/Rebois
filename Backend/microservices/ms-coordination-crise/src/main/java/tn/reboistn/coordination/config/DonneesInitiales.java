package tn.reboistn.coordination.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.reboistn.coordination.service.EscaladeService;

/** Crée les règles d'escalade par défaut au premier démarrage. */
@Component
@RequiredArgsConstructor
public class DonneesInitiales implements CommandLineRunner {

    private final EscaladeService escaladeService;

    @Override
    public void run(String... args) {
        escaladeService.initialiserRegles();
    }
}
