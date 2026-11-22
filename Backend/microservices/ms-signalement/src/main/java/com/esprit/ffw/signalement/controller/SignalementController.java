package com.esprit.ffw.signalement.controller;

import com.esprit.ffw.signalement.config.OpenApiConfig;
import com.esprit.ffw.signalement.dto.*;
import com.esprit.ffw.signalement.model.Signalement;
import com.esprit.ffw.signalement.model.Statut;
import com.esprit.ffw.signalement.service.SignalementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Signalements",
        description = "Déclaration, localisation, confirmation et suivi des départs de feu")
@RestController
@RequestMapping("/api/signalements")
@RequiredArgsConstructor
public class SignalementController {

    private final SignalementService service;

    private static final String SECURED = OpenApiConfig.BEARER;

    /** En profil dev (sans JWT) on utilise un utilisateur factice. */
    private String userId(Jwt jwt) { return jwt != null ? jwt.getSubject() : "dev-user"; }

    @Operation(
            summary = "Déclarer un départ de feu (GARDE, BENEVOLE)",
            description = "Crée un signalement, tente de le rattacher à un secteur forestier via ms-foret "
                    + "(repli en SECTEUR_A_VERIFIER si le service est indisponible) et renvoie les "
                    + "signalements actifs proches susceptibles d'être des doublons.",
            security = @SecurityRequirement(name = SECURED))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Signalement créé"),
            @ApiResponse(responseCode = "400", description = "Données invalides (coordonnées, type, JSON)",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "JWT absent ou invalide"),
            @ApiResponse(responseCode = "403", description = "Rôle GARDE ou BENEVOLE requis")
    })
    @PostMapping
    public ResponseEntity<SignalementResponse> creer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Caractéristiques du départ de feu observé", required = true)
            @Valid @RequestBody SignalementRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.creer(req, userId(jwt)));
    }

    @Operation(
            summary = "Rechercher les signalements (paginé)",
            description = "Filtre optionnellement par secteur forestier et/ou statut. "
                    + "Pagination Spring : `page`, `size`, `sort` (ex. `sort=creeLe,desc`).",
            security = @SecurityRequirement(name = SECURED))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Page de signalements"),
            @ApiResponse(responseCode = "400", description = "Filtre ou pagination invalide",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public Page<Signalement> rechercher(
            @Parameter(description = "Identifiant du secteur forestier", example = "14")
            @RequestParam(required = false) Long secteurId,
            @Parameter(description = "Statut du signalement", example = "CONFIRME")
            @RequestParam(required = false) Statut statut,
            @ParameterObject @PageableDefault(size = 20, sort = "creeLe",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        return service.rechercher(secteurId, statut, pageable);
    }

    @Operation(
            summary = "Signalements proches d'un point ($near)",
            description = "Recherche géospatiale MongoDB : signalements actifs dans un rayon donné "
                    + "autour de la position, créés pendant les 30 dernières minutes.",
            security = @SecurityRequirement(name = SECURED))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signalements actifs proches"),
            @ApiResponse(responseCode = "400", description = "Coordonnées GPS ou rayon invalides",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/proches")
    public List<Signalement> proches(
            @Parameter(description = "Latitude WGS84", example = "36.8065", required = true)
            @RequestParam double lat,
            @Parameter(description = "Longitude WGS84", example = "10.1815", required = true)
            @RequestParam double lng,
            @Parameter(description = "Rayon de recherche en mètres", example = "500")
            @RequestParam(defaultValue = "500") double rayonM) {
        if (lat < -90 || lat > 90 || lng < -180 || lng > 180 || rayonM <= 0) {
            throw new IllegalArgumentException("Coordonnées GPS ou rayon invalides");
        }
        return service.proches(lat, lng, rayonM, 30);
    }

    @Operation(
            summary = "Feux confirmés en cours (public)",
            description = "Liste des signalements au statut CONFIRME. Aucun jeton requis.")
    @ApiResponse(responseCode = "200", description = "Feux confirmés actifs")
    @GetMapping("/actifs")
    public List<Signalement> actifs() { return service.actifs(); }

    @Operation(
            summary = "Détail et historique d'un signalement",
            description = "Renvoie le signalement complet avec ses photos et l'historique des transitions.",
            security = @SecurityRequirement(name = SECURED))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signalement trouvé"),
            @ApiResponse(responseCode = "404", description = "Identifiant inconnu",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public Signalement detail(
            @Parameter(description = "Identifiant MongoDB du signalement",
                    example = "66f1c2a8e4b0a1f2c3d4e5f6", required = true)
            @PathVariable String id) { return service.trouver(id); }

    @Operation(
            summary = "Ajouter une photo (auteur uniquement)",
            description = "Ajoute une URL d'image au signalement. Seul l'auteur du signalement "
                    + "peut ajouter des photos, dans la limite de 10.",
            security = @SecurityRequirement(name = SECURED))
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Photo ajoutée"),
            @ApiResponse(responseCode = "400", description = "URL invalide ou limite de 10 photos atteinte",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "L'appelant n'est pas l'auteur du signalement",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Signalement introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/{id}/photos")
    public ResponseEntity<Signalement> photo(
            @Parameter(description = "Identifiant MongoDB du signalement", required = true)
            @PathVariable String id,
            @Valid @RequestBody PhotoRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.ajouterPhoto(id, req.url(), userId(jwt)));
    }

    @Operation(
            summary = "Confirmer le feu (GARDE)",
            description = "Passe le signalement à CONFIRME, historise la transition et publie "
                    + "l'événement `feu.confirme` via l'outbox.",
            security = @SecurityRequirement(name = SECURED))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signalement confirmé"),
            @ApiResponse(responseCode = "404", description = "Signalement introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Transition interdite ou conflit de concurrence",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}/confirmer")
    public Signalement confirmer(
            @Parameter(description = "Identifiant MongoDB du signalement", required = true)
            @PathVariable String id,
            @RequestBody(required = false) ConfirmationRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return service.confirmer(id, userId(jwt), req != null ? req.motif() : null);
    }

    @Operation(
            summary = "Classer en fausse alerte (GARDE)",
            description = "Passe le signalement à FAUSSE_ALERTE et historise le motif.",
            security = @SecurityRequirement(name = SECURED))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Signalement classé en fausse alerte"),
            @ApiResponse(responseCode = "404", description = "Signalement introuvable",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Transition interdite ou conflit de concurrence",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PatchMapping("/{id}/fausse-alerte")
    public Signalement fausseAlerte(
            @Parameter(description = "Identifiant MongoDB du signalement", required = true)
            @PathVariable String id,
            @RequestBody(required = false) ConfirmationRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        return service.fausseAlerte(id, userId(jwt), req != null ? req.motif() : null);
    }
}
