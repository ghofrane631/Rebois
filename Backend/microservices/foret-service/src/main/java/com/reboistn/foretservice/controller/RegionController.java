package com.reboistn.foretservice.controller;

import com.reboistn.foretservice.dto.request.RegionRequest;
import com.reboistn.foretservice.dto.response.DelegationResponse;
import com.reboistn.foretservice.dto.response.RegionResponse;
import com.reboistn.foretservice.service.DelegationService;
import com.reboistn.foretservice.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
@Tag(name = "Régions & Délégations", description = "Gestion des régions et délégations")
public class RegionController {

    private final RegionService regionService;
    private final DelegationService delegationService;

    @GetMapping
    @Operation(summary = "Lister toutes les régions")
    @ApiResponse(responseCode = "200", description = "Liste des régions")
    public ResponseEntity<List<RegionResponse>> findAll() {
        return ResponseEntity.ok(regionService.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une région par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Région trouvée"),
            @ApiResponse(responseCode = "404", description = "Région introuvable")
    })
    public ResponseEntity<RegionResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.findById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une région")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Région créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "409", description = "Nom déjà utilisé")
    })
    public ResponseEntity<RegionResponse> create(@Valid @RequestBody RegionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(regionService.create(request));
    }

    @GetMapping("/{id}/delegations")
    @Operation(summary = "Lister les délégations d'une région")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des délégations"),
            @ApiResponse(responseCode = "404", description = "Région introuvable")
    })
    public ResponseEntity<List<DelegationResponse>> findDelegationsByRegion(@PathVariable Long id) {
        return ResponseEntity.ok(delegationService.findByRegionId(id));
    }
}
