package tn.reboistn.coordination.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.reboistn.coordination.model.AlerteEmise;
import tn.reboistn.coordination.model.Escalade;
import tn.reboistn.coordination.repository.AlerteEmiseRepository;
import tn.reboistn.coordination.repository.EscaladeRepository;

import java.util.List;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
public class AlerteController {

    private final AlerteEmiseRepository alerteRepository;
    private final EscaladeRepository escaladeRepository;

    /** TODO S8 : l'identifiant viendra du jeton Keycloak au lieu de l'en-tête X-User-Id. */
    @GetMapping("/me")
    public List<AlerteEmise> mesAlertes(@RequestHeader("X-User-Id") String userId,
                                        @RequestParam(required = false) Boolean lue) {
        return lue == null ? alerteRepository.findByUserId(userId) : alerteRepository.findByUserIdAndLue(userId, lue);
    }

    @PatchMapping("/{id}/lue")
    public AlerteEmise marquerLue(@PathVariable Long id) {
        AlerteEmise alerte = trouver(id);
        if (alerte.isLue()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alerte deja lue");
        }
        alerte.setLue(true);
        return alerteRepository.save(alerte);
    }

    @GetMapping("/{id}/escalades")
    public List<Escalade> escalades(@PathVariable Long id) {
        trouver(id);
        return escaladeRepository.findByAlerteId(id);
    }

    private AlerteEmise trouver(Long id) {
        return alerteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerte introuvable"));
    }
}
