package tn.reboistn.coordination.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.reboistn.coordination.model.Abonnement;
import tn.reboistn.coordination.repository.AbonnementRepository;

import java.util.List;

@RestController
@RequestMapping("/api/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementRepository abonnementRepository;

    @GetMapping
    public List<Abonnement> lister(@RequestParam(required = false) String userId) {
        return userId == null ? abonnementRepository.findAll() : abonnementRepository.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Abonnement abonner(@Valid @RequestBody Abonnement abonnement) {
        if (abonnementRepository.existsByUserIdAndGouvernorat(abonnement.getUserId(), abonnement.getGouvernorat())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deja abonne a cette zone");
        }
        abonnement.setId(null);
        return abonnementRepository.save(abonnement);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desabonner(@PathVariable Long id) {
        if (!abonnementRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Abonnement introuvable");
        }
        abonnementRepository.deleteById(id);
    }
}
