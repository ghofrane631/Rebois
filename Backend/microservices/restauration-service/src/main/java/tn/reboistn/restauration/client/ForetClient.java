package tn.reboistn.restauration.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "foret-service", url = "${services.foret.url}", fallbackFactory = ForetClientFallbackFactory.class)
public interface ForetClient {

    @GetMapping("/api/secteurs/{secteurId}/vegetation")
    VegetationResponse getVegetationCible(@PathVariable Long secteurId);

    record VegetationResponse(String vegetationCible) {
    }
}