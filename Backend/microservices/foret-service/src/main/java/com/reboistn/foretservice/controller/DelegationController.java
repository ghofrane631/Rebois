package com.reboistn.foretservice.controller;

import com.reboistn.foretservice.dto.request.DelegationRequest;
import com.reboistn.foretservice.dto.response.DelegationResponse;
import com.reboistn.foretservice.service.DelegationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/delegations")
@RequiredArgsConstructor
@Tag(name = "Délégations", description = "Gestion des délégations")
public class DelegationController {

    private final DelegationService delegationService;

    @PostMapping
    @Operation(summary = "Créer une délégation")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Délégation créée"),
            @ApiResponse(responseCode = "400", description = "Données invalides"),
            @ApiResponse(responseCode = "404", description = "Région introuvable"),
            @ApiResponse(responseCode = "409", description = "Doublon dans la région")
    })
    public ResponseEntity<DelegationResponse> create(@Valid @RequestBody DelegationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(delegationService.create(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une délégation par ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Délégation trouvée"),
            @ApiResponse(responseCode = "404", description = "Délégation introuvable")
    })
    public ResponseEntity<DelegationResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(delegationService.findById(id));
    }
}
