package com.reboistn.foretservice.controller;

import com.reboistn.foretservice.dto.request.ZoneForestiereRequest;
import com.reboistn.foretservice.dto.response.ZoneForestiereResponse;
import com.reboistn.foretservice.entity.StatutZone;
import com.reboistn.foretservice.entity.TypeZone;
import com.reboistn.foretservice.service.ZoneForestiereService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
@Tag(name = "Zones Forestières", description = "CRUD et archivage des zones forestières")
public class ZoneForestiereController {

    private final ZoneForestiereService zoneService;

    @PostMapping
    @Operation(summary = "Créer une zone forestière")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Zone créée, Location dans l'en-tête"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Délégation introuvable"),
            @ApiResponse(responseCode = "409", description = "Nom déjà utilisé")
    })
    public ResponseEntity<ZoneForestiereResponse> create(@Valid @RequestBody ZoneForestiereRequest request) {
        ZoneForestiereResponse created = zoneService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une zone forestière")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zone mise à jour"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Zone introuvable"),
            @ApiResponse(responseCode = "409", description = "Nom déjà utilisé")
    })
    public ResponseEntity<ZoneForestiereResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ZoneForestiereRequest request) {
        return ResponseEntity.ok(zoneService.update(id, request));
    }

    @GetMapping
    @Operation(summary = "Lister les zones forestières (paginé, filtrable)")
    @ApiResponse(responseCode = "200", description = "Page de zones (ACTIVE par défaut, ARCHIVEE exclues)")
    public ResponseEntity<Page<ZoneForestiereResponse>> findAll(
            @Parameter(description = "Numéro de page (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Taille de la page") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filtre par type") @RequestParam(required = false) TypeZone type,
            @Parameter(description = "Filtre par statut (ACTIVE par défaut)") @RequestParam(required = false) StatutZone statut,
            @Parameter(description = "Filtre par délégation") @RequestParam(required = false) Long delegationId,
            @Parameter(description = "Filtre par région") @RequestParam(required = false) Long regionId
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateCreation").descending());
        return ResponseEntity.ok(zoneService.findAll(type, statut, delegationId, regionId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une zone forestière par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zone trouvée"),
            @ApiResponse(responseCode = "404", description = "Zone introuvable")
    })
    public ResponseEntity<ZoneForestiereResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.findById(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Archiver une zone forestière (archivage logique, non suppression physique)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Zone archivée"),
            @ApiResponse(responseCode = "404", description = "Zone introuvable")
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        zoneService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Archiver explicitement une zone (statut → ARCHIVEE)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Zone archivée"),
            @ApiResponse(responseCode = "404", description = "Zone introuvable")
    })
    public ResponseEntity<ZoneForestiereResponse> archive(@PathVariable Long id) {
        return ResponseEntity.ok(zoneService.archive(id));
    }
}
