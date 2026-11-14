package com.esprit.ffw.signalement.controller;

import com.esprit.ffw.signalement.dto.*;
import com.esprit.ffw.signalement.model.Signalement;
import com.esprit.ffw.signalement.model.Statut;
import com.esprit.ffw.signalement.service.SignalementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Signalements")
@RestController
@RequestMapping("/api/signalements")
@RequiredArgsConstructor
public class SignalementController {

    private final SignalementService service;

    /** En profil dev (sans JWT) on utilise un utilisateur factice. */
    private String userId(Jwt jwt) { return jwt != null ? jwt.getSubject() : "dev-user"; }

    @Operation(summary = "Signaler un feu (GARDE, BENEVOLE)")
    @PostMapping
    public ResponseEntity<SignalementResponse> creer(@Valid @RequestBody SignalementRequest req,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(req, userId(jwt)));
    }

    @Operation(summary = "Rechercher par secteur et/ou statut")
    @GetMapping
    public List<Signalement> rechercher(@RequestParam(required = false) Long secteurId,
                                        @RequestParam(required = false) Statut statut) {
        return service.rechercher(secteurId, statut);
    }

    @Operation(summary = "Signalements proches ($near)")
    @GetMapping("/proches")
    public List<Signalement> proches(@RequestParam double lat, @RequestParam double lng,
                                     @RequestParam(defaultValue = "500") double rayonM) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180 || rayonM <= 0) {
            throw new IllegalArgumentException("Coordonnées GPS ou rayon invalides");
        }
        return service.proches(lat, lng, rayonM, 30);
    }

    @Operation(summary = "Feux confirmés en cours (public)")
    @GetMapping("/actifs")
    public List<Signalement> actifs() { return service.actifs(); }

    @Operation(summary = "Détail et historique")
    @GetMapping("/{id}")
    public Signalement detail(@PathVariable String id) { return service.trouver(id); }

    @Operation(summary = "Ajouter une photo (auteur uniquement)")
    @PostMapping("/{id}/photos")
    public ResponseEntity<Signalement> photo(@PathVariable String id, @Valid @RequestBody PhotoRequest req,
                                             @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ajouterPhoto(id, req.url(), userId(jwt)));
    }

    @Operation(summary = "Confirmer le feu (GARDE)")
    @PatchMapping("/{id}/confirmer")
    public Signalement confirmer(@PathVariable String id,
                                 @RequestBody(required = false) ConfirmationRequest req,
                                 @AuthenticationPrincipal Jwt jwt) {
        return service.confirmer(id, userId(jwt), req != null ? req.motif() : null);
    }

    @Operation(summary = "Classer en fausse alerte (GARDE)")
    @PatchMapping("/{id}/fausse-alerte")
    public Signalement fausseAlerte(@PathVariable String id,
                                    @RequestBody(required = false) ConfirmationRequest req,
                                    @AuthenticationPrincipal Jwt jwt) {
        return service.fausseAlerte(id, userId(jwt), req != null ? req.motif() : null);
    }
}
