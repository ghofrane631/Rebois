package tn.reboistn.coordination.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.reboistn.coordination.dto.*;
import tn.reboistn.coordination.model.*;
import tn.reboistn.coordination.service.IncidentService;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Lutte contre un feu : moyens, chronologie, statut et bilan")
public class IncidentController {

    private final IncidentService incidentService;

    @GetMapping
    @Operation(summary = "Lister les incidents", description = "200")
    public List<Incident> lister(@RequestParam(required = false) StatutIncident statut) {
        return incidentService.lister(statut);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Détail : chronologie, moyens, alertes, bilan", description = "200, 404")
    public IncidentDetail detail(@PathVariable Long id) {
        return incidentService.detail(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ouvrir un incident (alerte la zone et réserve les premiers moyens)",
            description = "201, 400, 409 si un incident existe déjà pour ce signalement")
    public Incident ouvrir(@Valid @RequestBody Incident incident) {
        return incidentService.ouvrir(incident);
    }

    @PostMapping("/{id}/moyens")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Réserver des moyens auprès de Ressources (OpenFeign)",
            description = "201, 400, 404, 409 aucun moyen disponible, 503 Ressources indisponible")
    public List<MoyenEngage> engagerMoyens(@PathVariable Long id, @Valid @RequestBody MoyenRequest request) {
        return incidentService.engagerMoyens(id, request.type(), request.nombre());
    }

    @PostMapping("/{id}/chronologie")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Ajouter une action à la chronologie", description = "201, 400, 404, 409")
    public EvenementChrono ajouterAction(@PathVariable Long id, @Valid @RequestBody ChronoRequest request) {
        return incidentService.ajouterAction(id, request.action());
    }

    @PatchMapping("/{id}/statut")
    @Operation(summary = "Changer le statut (EN_COURS ou MAITRISE)",
            description = "200, 400, 404, 409 transition interdite ou CLOS demandé")
    public Incident changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest request) {
        return incidentService.changerStatut(id, request.statut());
    }

    @PutMapping("/{id}/bilan")
    @Operation(summary = "Enregistrer le bilan et clore (publie InterventionTerminee)",
            description = "200, 400, 404, 409 si le feu n'est pas MAITRISE")
    public Bilan cloturer(@PathVariable Long id, @Valid @RequestBody BilanRequest request) {
        return incidentService.cloturer(id, request);
    }

    @GetMapping("/{id}/alertes")
    @Operation(summary = "Alertes émises pour un incident", description = "200, 404")
    public List<AlerteEmise> alertes(@PathVariable Long id) {
        return incidentService.alertes(id);
    }
}
