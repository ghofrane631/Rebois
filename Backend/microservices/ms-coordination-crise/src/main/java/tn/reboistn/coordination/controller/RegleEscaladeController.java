package tn.reboistn.coordination.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tn.reboistn.coordination.model.RegleEscalade;
import tn.reboistn.coordination.service.EscaladeService;

import java.util.List;

@RestController
@RequestMapping("/api/regles-escalade")
@RequiredArgsConstructor
@Tag(name = "Règles d'escalade", description = "Délai avant escalade d'une alerte non lue")
public class RegleEscaladeController {

    private final EscaladeService escaladeService;

    @GetMapping
    @Operation(summary = "Lister les règles", description = "200")
    public List<RegleEscalade> lister() {
        return escaladeService.regles();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Créer une règle", description = "201, 400, 409 si le type a déjà une règle")
    public RegleEscalade creer(@Valid @RequestBody RegleEscalade regle) {
        return escaladeService.creerRegle(regle);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier une règle", description = "200, 400, 404")
    public RegleEscalade modifier(@PathVariable Long id, @Valid @RequestBody RegleEscalade regle) {
        return escaladeService.modifierRegle(id, regle);
    }
}
