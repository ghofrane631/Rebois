package tn.reboistn.coordination.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.reboistn.coordination.model.Abonnement;
import tn.reboistn.coordination.service.AlerteService;

import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
@Tag(name = "Abonnements", description = "Abonnement aux alertes d'une zone")
public class AbonnementController {

    private final AlerteService alerteService;

    @GetMapping
    @Operation(summary = "Lister les abonnements", description = "200")
    public List<Abonnement> lister(@RequestParam(required = false) String userId) {
        return alerteService.abonnements(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "S'abonner à une zone", description = "201, 400, 409 déjà abonné")
    public Abonnement abonner(@Valid @RequestBody Abonnement abonnement) {
        return alerteService.abonner(abonnement);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Se désabonner", description = "204, 404")
    public void desabonner(@PathVariable Long id) {
        alerteService.desabonner(id);
    }
}
