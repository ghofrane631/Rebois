package tn.reboistn.coordination.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Appel synchrone vers ms-user : utilisateurs d'une zone, destinataires des alertes. */
@FeignClient(name = "${clients.user:ms-user}", contextId = "userClient")
public interface UserClient {

    @GetMapping("/api/users")
    List<Utilisateur> parZone(@RequestParam("gouvernorat") String gouvernorat);

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Utilisateur(String id, String role) {
    }
}
