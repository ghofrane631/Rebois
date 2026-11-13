package tn.reboistn.coordination.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.reboistn.coordination.model.AlerteEmise;
import tn.reboistn.coordination.model.Escalade;
import tn.reboistn.coordination.service.AlerteService;

import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
@Tag(name = "Alertes", description = "Alertes reçues, accusé de réception et escalades")
public class AlerteController {

    private final AlerteService alerteService;

    /** TODO S8 : l'identifiant viendra du jeton Keycloak au lieu de l'en-tête X-User-Id. */
    @GetMapping("/me")
    @Operation(summary = "Mes alertes", description = "200, 400 sans en-tête X-User-Id")
    public List<AlerteEmise> mesAlertes(@RequestHeader("X-User-Id") String userId,
                                        @RequestParam(required = false) Boolean lue) {
        return alerteService.mesAlertes(userId, lue);
    }

    @PatchMapping("/{id}/lue")
    @Operation(summary = "Accuser réception", description = "200, 404, 409 déjà lue")
    public AlerteEmise marquerLue(@PathVariable Long id) {
        return alerteService.marquerLue(id);
    }

    @GetMapping("/{id}/escalades")
    @Operation(summary = "Escalades d'une alerte", description = "200, 404")
    public List<Escalade> escalades(@PathVariable Long id) {
        return alerteService.escalades(id);
    }
}
