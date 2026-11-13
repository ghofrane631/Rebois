package tn.reboistn.coordination.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/** Appel synchrone vers ms-signalement : position et intensité du feu. */
@FeignClient(name = "${clients.signalement:ms-signalement}", contextId = "signalementClient")
public interface SignalementClient {

    @GetMapping("/api/signalements/{id}")
    Signalement detail(@PathVariable("id") Long id);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Signalement(Long id, Long secteurId, Double latitude, Double longitude, Integer intensite) {
    }
}
