package tn.reboistn.coordination.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reboistn.coordination.event.FeuConfirme;
import tn.reboistn.coordination.event.MoyensInsuffisants;
import tn.reboistn.coordination.event.NiveauRisqueChange;
import tn.reboistn.coordination.event.UtilisateurCree;
import tn.reboistn.coordination.model.EvenementTraite;
import tn.reboistn.coordination.model.Incident;
import tn.reboistn.coordination.model.Niveau;
import tn.reboistn.coordination.model.SourceAction;
import tn.reboistn.coordination.repository.EvenementTraiteRepository;
import tn.reboistn.coordination.repository.IncidentRepository;

/**
 * Réaction aux événements reçus par RabbitMQ.
 * Chaque événement porte un eventId : un message reçu deux fois n'est traité qu'une fois.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EvenementService {

    private final EvenementTraiteRepository evenementTraiteRepository;
    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;
    private final AlerteService alerteService;

    /** Un feu confirmé ouvre un incident (une seule fois par signalement). */
    @Transactional
    public void surFeuConfirme(FeuConfirme e) {
        if (dejaTraite(e.eventId(), "FeuConfirme") || e.signalementId() == null) {
            return;
        }
        if (incidentRepository.existsBySignalementId(e.signalementId())) {
            log.info("Incident deja ouvert pour le signalement {}", e.signalementId());
            return;
        }
        Incident incident = new Incident();
        incident.setSignalementId(e.signalementId());
        incident.setSecteurId(e.secteurId());
        incident.setGouvernorat(e.gouvernorat() != null ? e.gouvernorat() : "A_PRECISER");
        incident.setLatitude(e.latitude());
        incident.setLongitude(e.longitude());
        incident.setNiveauRisque(niveau(e.niveauRisque(), Niveau.ELEVE));
        incidentService.ouvrir(incident);
    }

    /** Le risque d'une zone monte : alerte de vigilance à partir du niveau ELEVE. */
    @Transactional
    public void surNiveauRisqueChange(NiveauRisqueChange e) {
        if (dejaTraite(e.eventId(), "NiveauRisqueChange")) {
            return;
        }
        Niveau niveau = niveau(e.nouveauNiveau(), null);
        if (niveau == null || niveau.compareTo(Niveau.ELEVE) < 0) {
            return;
        }
        if (e.gouvernorat() == null) {
            log.warn("NiveauRisqueChange sans gouvernorat pour le secteur {} : alerte de vigilance impossible", e.secteurId());
            return;
        }
        alerteService.alerterVigilance(e.gouvernorat(), niveau);
    }

    /** Ressources n'a plus de moyens libres : alerte régionale et trace dans la chronologie. */
    @Transactional
    public void surMoyensInsuffisants(MoyensInsuffisants e) {
        if (dejaTraite(e.eventId(), "MoyensInsuffisants")) {
            return;
        }
        Incident incident = e.incidentId() == null ? null : incidentRepository.findById(e.incidentId()).orElse(null);
        String gouvernorat = e.gouvernorat() != null ? e.gouvernorat()
                : incident != null ? incident.getGouvernorat() : null;
        if (gouvernorat == null) {
            log.warn("MoyensInsuffisants sans zone connue (incident {})", e.incidentId());
            return;
        }
        alerteService.alerterMoyensInsuffisants(incident, gouvernorat);
        if (incident != null) {
            incidentService.tracer(incident, "Moyens insuffisants : " + e.typeMoyen() + " (alerte regionale)",
                    SourceAction.SYSTEME);
        }
    }

    /** Nouvel utilisateur : abonnement par défaut aux alertes de sa zone. */
    @Transactional
    public void surUtilisateurCree(UtilisateurCree e) {
        if (dejaTraite(e.eventId(), "UtilisateurCree") || e.userId() == null || e.gouvernorat() == null) {
            return;
        }
        alerteService.abonnementParDefaut(e.userId(), e.gouvernorat());
    }

    /** Sans eventId, l'événement ne peut pas être dédoublonné : il est traité. */
    private boolean dejaTraite(String eventId, String type) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }
        if (evenementTraiteRepository.existsById(eventId)) {
            log.info("{} {} deja traite, ignore", type, eventId);
            return true;
        }
        evenementTraiteRepository.save(new EvenementTraite(eventId, type));
        return false;
    }

    private Niveau niveau(String valeur, Niveau parDefaut) {
        if (valeur == null) {
            return parDefaut;
        }
        try {
            return Niveau.valueOf(valeur);
        } catch (IllegalArgumentException ex) {
            return parDefaut;
        }
    }
}
