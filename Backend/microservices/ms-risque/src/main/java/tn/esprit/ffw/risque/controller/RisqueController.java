package tn.esprit.ffw.risque.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tn.esprit.ffw.risque.dto.*;
import tn.esprit.ffw.risque.entity.NiveauRisque;
import tn.esprit.ffw.risque.service.RisqueService;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/risque")
@RequiredArgsConstructor
public class RisqueController {

    private final RisqueService service;

    @PostMapping("/releves")                       // GARDE -> 201, 400, 404, 503
    @ResponseStatus(HttpStatus.CREATED)
    public ReleveResponse creerReleve(@Valid @RequestBody ReleveRequest req, Authentication auth) {
        return service.enregistrerReleve(req, auth != null ? auth.getName() : null);
    }

    @GetMapping("/secteurs/{id}")                  // public
    public IndiceResponse indice(@PathVariable Long id) { return service.indiceSecteur(id); }

    @GetMapping("/carte")                          // public
    public List<IndiceResponse> carte(@RequestParam(required = false) NiveauRisque niveau) {
        return service.carte(niveau);
    }

    @GetMapping("/releves")                        // authentifié
    public List<ReleveResponse> releves(
            @RequestParam(required = false) Long secteurId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
        return service.releves(secteurId, from, to);
    }

    @GetMapping("/secteurs/{id}/historique")       // authentifié
    public List<HistoriqueResponse> historique(@PathVariable Long id) { return service.historique(id); }

    @PutMapping("/seuils")                         // ADMIN
    public List<SeuilDto> seuils(@RequestBody @Valid List<SeuilDto> seuils) {
        return service.mettreAJourSeuils(seuils);
    }
}
