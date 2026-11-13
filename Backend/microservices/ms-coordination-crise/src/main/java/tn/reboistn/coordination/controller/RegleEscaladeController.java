package tn.reboistn.coordination.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.reboistn.coordination.model.RegleEscalade;
import tn.reboistn.coordination.repository.RegleEscaladeRepository;

import java.util.List;

@RestController
@RequestMapping("/api/regles-escalade")
@RequiredArgsConstructor
public class RegleEscaladeController {

    private final RegleEscaladeRepository regleRepository;

    @GetMapping
    public List<RegleEscalade> lister() {
        return regleRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RegleEscalade creer(@Valid @RequestBody RegleEscalade regle) {
        if (regleRepository.existsByTypeAlerte(regle.getTypeAlerte())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une regle existe deja pour ce type d'alerte");
        }
        regle.setId(null);
        return regleRepository.save(regle);
    }

    /** Le type d'alerte d'une règle ne change pas : on modifie le délai, la cible et l'obligation de SMS. */
    @PutMapping("/{id}")
    public RegleEscalade modifier(@PathVariable Long id, @Valid @RequestBody RegleEscalade regle) {
        RegleEscalade existante = regleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regle introuvable"));
        existante.setDelaiMin(regle.getDelaiMin());
        existante.setCible(regle.getCible());
        existante.setSmsObligatoire(regle.isSmsObligatoire());
        return regleRepository.save(existante);
    }
}
