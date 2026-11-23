package tn.esprit.forestfirewatch.ressources.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.RessourceRequest;
import tn.esprit.forestfirewatch.ressources.entity.Ressource;
import tn.esprit.forestfirewatch.ressources.entity.StatutRessource;
import tn.esprit.forestfirewatch.ressources.exception.ApiException;
import tn.esprit.forestfirewatch.ressources.repository.RessourceRepository;

import java.util.List;

/** Recensement des moyens et changement de leur statut (maintenance, retour). */
@Service
public class RessourceService {

    private final RessourceRepository ressourceRepository;

    public RessourceService(RessourceRepository ressourceRepository) {
        this.ressourceRepository = ressourceRepository;
    }

    @Transactional
    public Ressource recenser(RessourceRequest req) {
        return ressourceRepository.save(new Ressource(req.type(), req.uniteId(), req.gouvernorat(),
                req.latitude(), req.longitude()));
    }

    @Transactional(readOnly = true)
    public List<Ressource> lister(String gouvernorat, StatutRessource statut) {
        if (gouvernorat != null && statut != null) {
            return ressourceRepository.findByGouvernoratAndStatut(gouvernorat, statut);
        }
        if (gouvernorat != null) {
            return ressourceRepository.findByGouvernorat(gouvernorat);
        }
        if (statut != null) {
            return ressourceRepository.findByStatut(statut);
        }
        return ressourceRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Ressource trouver(Long id) {
        return ressourceRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("RESSOURCE_INCONNUE", "Aucun moyen avec l'id " + id));
    }

    /**
     * Règles : un moyen RESERVEE ne part pas en maintenance (409) ;
     * le statut RESERVEE ne s'obtient que par une réservation (409).
     */
    @Transactional
    public Ressource changerStatut(Long id, StatutRessource nouveau) {
        Ressource r = trouver(id);
        if (nouveau == StatutRessource.RESERVEE) {
            throw ApiException.conflict("TRANSITION_INVALIDE",
                    "Utiliser POST /api/ressources/reservations pour réserver un moyen");
        }
        if (r.getStatut() == StatutRessource.RESERVEE) {
            throw ApiException.conflict("RESSOURCE_DEJA_RESERVEE",
                    "Le moyen " + id + " est engagé sur un incident : le libérer d'abord");
        }
        r.setStatut(nouveau);
        return r;
    }
}
