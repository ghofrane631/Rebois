package tn.esprit.forestfirewatch.ressources.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.forestfirewatch.ressources.entity.*;
import tn.esprit.forestfirewatch.ressources.event.Evenements.InterventionTerminee;
import tn.esprit.forestfirewatch.ressources.event.Evenements.MoyensInsuffisants;
import tn.esprit.forestfirewatch.ressources.event.Evenements.NiveauRisqueChange;
import tn.esprit.forestfirewatch.ressources.repository.EvenementTraiteRepository;
import tn.esprit.forestfirewatch.ressources.repository.PrePositionnementRepository;
import tn.esprit.forestfirewatch.ressources.repository.RessourceRepository;

import java.util.Comparator;
import java.util.List;

/**
 * Traitement des événements consommés. Chaque traitement est idempotent :
 * l'eventId est enregistré dans la même transaction que l'effet.
 */
@Service
public class EvenementService {

    private static final Logger log = LoggerFactory.getLogger(EvenementService.class);

    private final EvenementTraiteRepository evenementTraiteRepository;
    private final RessourceRepository ressourceRepository;
    private final PrePositionnementRepository prePositionnementRepository;
    private final ReservationService reservationService;
    private final OutboxService outboxService;
    private final int reserveMinimale;

    public EvenementService(EvenementTraiteRepository evenementTraiteRepository,
                            RessourceRepository ressourceRepository,
                            PrePositionnementRepository prePositionnementRepository,
                            ReservationService reservationService,
                            OutboxService outboxService,
                            @Value("${ressources.reserve-minimale-camions:1}") int reserveMinimale) {
        this.evenementTraiteRepository = evenementTraiteRepository;
        this.ressourceRepository = ressourceRepository;
        this.prePositionnementRepository = prePositionnementRepository;
        this.reservationService = reservationService;
        this.outboxService = outboxService;
        this.reserveMinimale = reserveMinimale;
    }

    /** InterventionTerminee : libère les moyens réservés pour l'incident. */
    @Transactional
    public void surInterventionTerminee(InterventionTerminee e) {
        if (dejaTraite(e.eventId(), "InterventionTerminee")) {
            return;
        }
        int liberes = reservationService.libererIncident(e.incidentId());
        log.info("InterventionTerminee incident={} : {} moyen(s) libéré(s)", e.incidentId(), liberes);
    }

    /**
     * NiveauRisqueChange : si le secteur passe en EXTRÊME, on pré-positionne le camion-citerne
     * libre le plus proche, sans vider sa caserne. Sinon on publie MoyensInsuffisants.
     */
    @Transactional
    public void surNiveauRisqueChange(NiveauRisqueChange e) {
        if (dejaTraite(e.eventId(), "NiveauRisqueChange")) {
            return;
        }
        if (!estExtreme(e.nouveauNiveau())) {
            return;
        }
        if (!prePositionnementRepository.findBySecteurId(e.secteurId()).isEmpty()) {
            return; // un moyen est déjà pré-positionné sur ce secteur
        }

        List<Ressource> candidats = ressourceRepository
                .findByTypeAndStatut(TypeRessource.CAMION_CITERNE, StatutRessource.DISPONIBLE).stream()
                .filter(r -> ressourceRepository.countByUniteIdAndTypeAndStatut(
                        r.getUniteId(), TypeRessource.CAMION_CITERNE, StatutRessource.DISPONIBLE) - 1 >= reserveMinimale)
                .filter(r -> e.latitude() != null || r.getGouvernorat().equalsIgnoreCase(e.gouvernorat()))
                .sorted(Comparator.comparingDouble(r -> e.latitude() == null ? 0
                        : Distance.km(e.latitude(), e.longitude(), r.getLatitude(), r.getLongitude())))
                .toList();

        if (candidats.isEmpty()) {
            outboxService.enregistrer("MoyensInsuffisants", new MoyensInsuffisants(null, e.secteurId(),
                    e.gouvernorat(), TypeRessource.CAMION_CITERNE.name(), 1, 0, "PRE_POSITIONNEMENT_IMPOSSIBLE"));
            log.warn("Risque EXTRÊME secteur={} : aucun camion à pré-positionner", e.secteurId());
            return;
        }
        PrePositionnement p = prePositionnementRepository.save(new PrePositionnement(candidats.get(0), e.secteurId()));
        log.info("Risque EXTRÊME secteur={} : camion {} pré-positionné", e.secteurId(), p.getRessource().getId());
    }

    private boolean dejaTraite(String eventId, String type) {
        if (eventId == null || evenementTraiteRepository.existsById(eventId)) {
            log.info("{} {} déjà traité ou sans eventId : ignoré", type, eventId);
            return true;
        }
        evenementTraiteRepository.save(new EvenementTraite(eventId, type));
        return false;
    }

    private static boolean estExtreme(String niveau) {
        return niveau != null && niveau.replace("Ê", "E").equalsIgnoreCase("EXTREME");
    }
}
