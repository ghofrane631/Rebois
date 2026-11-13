package tn.reboistn.coordination.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import tn.reboistn.coordination.model.TypeMoyen;

import java.util.List;

/** Appel synchrone vers ms-ressources : réserver les moyens les plus proches du feu. */
@FeignClient(name = "${clients.ressources:ressources-service}", contextId = "ressourcesClient")
public interface RessourcesClient {

    @PostMapping("/api/ressources/reservations")
    ReservationResultat reserver(@RequestBody ReservationRequest request);

    record ReservationRequest(Long incidentId, TypeMoyen type, Integer nombre, Double lat, Double lng) {
    }

    record ReservationResultat(Long incidentId, List<Long> ressources, List<Reservation> reservations) {
    }

    record Reservation(Long id, Long ressourceId, TypeMoyen type) {
    }
}
