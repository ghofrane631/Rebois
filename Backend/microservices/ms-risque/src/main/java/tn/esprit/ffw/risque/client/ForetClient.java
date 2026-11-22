package tn.esprit.ffw.risque.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import tn.esprit.ffw.risque.dto.SecteurDto;

/** Appel SYNCHRONE vers le service Forêt (découvert via Eureka) pour vérifier qu'un secteur existe. */
@FeignClient(name = "ms-foret", fallbackFactory = ForetClientFallbackFactory.class)
public interface ForetClient {
    @GetMapping("/api/foret/secteurs/{id}")
    SecteurDto getSecteur(@PathVariable("id") Long id);
}
