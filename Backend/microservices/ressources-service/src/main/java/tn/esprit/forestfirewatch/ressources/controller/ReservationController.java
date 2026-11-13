package tn.esprit.forestfirewatch.ressources.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.ReservationRequest;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.ReservationResponse;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.ReservationResultat;
import tn.esprit.forestfirewatch.ressources.service.ReservationService;

import java.util.List;

/** Endpoints appelés par Coordination de crise (OpenFeign). */
@RestController
@RequestMapping("/api/ressources/reservations")
@Tag(name = "Réservations", description = "Réservation et libération des moyens pour un incident")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Réserver les moyens les plus proches du feu",
            description = "201 ; 400 requête invalide ; 409 AUCUN_MOYEN_DISPONIBLE (MoyensInsuffisants publié)")
    public ReservationResultat reserver(@Valid @RequestBody ReservationRequest req) {
        return reservationService.reserver(req);
    }

    @GetMapping
    @Operation(summary = "Moyens réservés pour un incident", description = "200")
    public List<ReservationResponse> parIncident(@RequestParam Long incidentId) {
        return reservationService.parIncident(incidentId).stream().map(ReservationResponse::of).toList();
    }

    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Libérer une réservation", description = "204, 404")
    public void liberer(@PathVariable Long id) {
        reservationService.liberer(id);
    }
}
