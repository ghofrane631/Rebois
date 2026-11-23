package tn.esprit.forestfirewatch.ressources.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.RessourceRequest;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.RessourceResponse;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.StatutRequest;
import tn.esprit.forestfirewatch.ressources.entity.StatutRessource;
import tn.esprit.forestfirewatch.ressources.service.RessourceService;

import java.util.List;

@RestController
@RequestMapping("/api/ressources")
@Tag(name = "Moyens", description = "Recensement et disponibilité des moyens de lutte")
public class RessourceController {

    private final RessourceService ressourceService;

    public RessourceController(RessourceService ressourceService) {
        this.ressourceService = ressourceService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Recenser un moyen (ADMIN)", description = "201, 400")
    public RessourceResponse recenser(@Valid @RequestBody RessourceRequest req) {
        return RessourceResponse.of(ressourceService.recenser(req));
    }

    @GetMapping
    @Operation(summary = "Moyens par gouvernorat et statut", description = "200")
    public List<RessourceResponse> lister(@RequestParam(required = false) String gouvernorat,
                                          @RequestParam(required = false) StatutRessource statut) {
        return ressourceService.lister(gouvernorat, statut).stream().map(RessourceResponse::of).toList();
    }

    @GetMapping("/{id:\\d+}")
    @Operation(summary = "Détail d'un moyen", description = "200, 404")
    public RessourceResponse detail(@PathVariable Long id) {
        return RessourceResponse.of(ressourceService.trouver(id));
    }

    @PatchMapping("/{id:\\d+}/statut")
    @Operation(summary = "Maintenance ou retour (PROTECTION_CIVILE)", description = "200, 404, 409")
    public RessourceResponse changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest req) {
        return RessourceResponse.of(ressourceService.changerStatut(id, req.statut()));
    }
}
