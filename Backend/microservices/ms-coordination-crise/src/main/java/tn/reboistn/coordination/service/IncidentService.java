package tn.reboistn.coordination.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.reboistn.coordination.client.RessourcesClient;
import tn.reboistn.coordination.client.RessourcesClient.ReservationRequest;
import tn.reboistn.coordination.client.RessourcesClient.ReservationResultat;
import tn.reboistn.coordination.client.SignalementClient;
import tn.reboistn.coordination.dto.BilanRequest;
import tn.reboistn.coordination.dto.IncidentDetail;
import tn.reboistn.coordination.event.InterventionTerminee;
import tn.reboistn.coordination.messaging.EvenementPublisher;
import tn.reboistn.coordination.model.*;
import tn.reboistn.coordination.repository.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Cycle de vie d'un incident : ouverture, moyens engagés, chronologie, statut et clôture par le bilan. */
@Slf4j
@Service
@RequiredArgsConstructor
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final MoyenEngageRepository moyenRepository;
    private final EvenementChronoRepository chronoRepository;
    private final BilanRepository bilanRepository;
    private final AlerteEmiseRepository alerteRepository;
    private final AlerteService alerteService;
    private final RessourcesClient ressourcesClient;
    private final SignalementClient signalementClient;
    private final EvenementPublisher publisher;

    @Value("${incident.moyens-initiaux.type}")
    private TypeMoyen typeMoyenInitial;

    @Value("${incident.moyens-initiaux.nombre}")
    private int nombreMoyensInitial;

    public List<Incident> lister(StatutIncident statut) {
        return statut == null ? incidentRepository.findAll() : incidentRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public IncidentDetail detail(Long id) {
        Incident incident = trouver(id);
        return new IncidentDetail(
                incident,
                chronoRepository.findByIncidentIdOrderByHeureAsc(id),
                moyenRepository.findByIncidentId(id),
                alerteRepository.findByIncident_Id(id),
                bilanRepository.findById(id).orElse(null));
    }

    /**
     * Ouvre un incident pour un feu confirmé : alerte la zone puis réserve les premiers moyens.
     * Appelé par l'API et à la réception de FeuConfirme.
     */
    public Incident ouvrir(Incident incident) {
        if (incidentRepository.existsBySignalementId(incident.getSignalementId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un incident existe deja pour ce signalement");
        }
        incident.setId(null);
        incident.setStatut(StatutIncident.OUVERT);
        incident.setOuvertLe(LocalDateTime.now());
        completerPosition(incident);

        Incident cree = incidentRepository.save(incident);
        tracer(cree, "Incident ouvert pour le signalement " + cree.getSignalementId(), SourceAction.SYSTEME);
        alerteService.alerterZone(cree);

        try {
            reserver(cree, typeMoyenInitial, nombreMoyensInitial);
        } catch (ResponseStatusException e) {
            // l'incident reste ouvert même sans moyens : on ne perd jamais un feu
            log.warn("Incident {} ouvert sans moyens : {}", cree.getId(), e.getReason());
        }
        return trouver(cree.getId());
    }

    /** Réserve des moyens supplémentaires auprès de ms-ressources. */
    public List<MoyenEngage> engagerMoyens(Long id, TypeMoyen type, int nombre) {
        return reserver(trouverNonClos(id), type, nombre);
    }

    public EvenementChrono ajouterAction(Long id, String action) {
        return tracer(trouverNonClos(id), action, SourceAction.AGENT);
    }

    @Transactional
    public Incident changerStatut(Long id, StatutIncident cible) {
        Incident incident = trouverNonClos(id);
        if (cible == StatutIncident.CLOS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cloture se fait par le bilan");
        }
        if (!incident.getStatut().peutPasserA(cible)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Transition interdite : " + incident.getStatut() + " -> " + cible);
        }
        incident.setStatut(cible);
        tracer(incident, "Statut passe a " + cible, SourceAction.AGENT);
        return incident;
    }

    /** Enregistre le bilan, clôt l'incident et prévient les autres services (InterventionTerminee). */
    @Transactional
    public Bilan cloturer(Long id, BilanRequest request) {
        Incident incident = trouverNonClos(id);
        if (incident.getStatut() != StatutIncident.MAITRISE) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Le feu doit etre MAITRISE avant le bilan");
        }
        Bilan bilan = new Bilan();
        bilan.setIncident(incident);
        bilan.setSurfaceBruleeHa(request.surfaceBruleeHa());
        bilan.setDureeMin(request.dureeMin());
        bilan.setCause(request.cause());
        Bilan enregistre = bilanRepository.save(bilan);

        incident.setStatut(StatutIncident.CLOS);
        tracer(incident, "Incident clos avec le bilan", SourceAction.SYSTEME);

        publisher.interventionTerminee(new InterventionTerminee(
                UUID.randomUUID().toString(), incident.getId(), incident.getSignalementId(),
                incident.getSecteurId(), request.surfaceBruleeHa(), LocalDateTime.now()));
        return enregistre;
    }

    public List<AlerteEmise> alertes(Long id) {
        trouver(id);
        return alerteRepository.findByIncident_Id(id);
    }

    public EvenementChrono tracer(Incident incident, String action, SourceAction source) {
        EvenementChrono chrono = new EvenementChrono();
        chrono.setIncident(incident);
        chrono.setHeure(LocalDateTime.now());
        chrono.setAction(action);
        chrono.setSource(source);
        return chronoRepository.save(chrono);
    }

    public Incident trouver(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident introuvable"));
    }

    // ---------- méthodes internes ----------

    private Incident trouverNonClos(Long id) {
        Incident incident = trouver(id);
        if (incident.getStatut() == StatutIncident.CLOS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Incident deja clos");
        }
        return incident;
    }

    /** Si la position manque, on la demande à ms-signalement. Sans réponse, l'incident est ouvert quand même. */
    private void completerPosition(Incident incident) {
        if (incident.getLatitude() != null && incident.getLongitude() != null) {
            return;
        }
        try {
            SignalementClient.Signalement s = signalementClient.detail(incident.getSignalementId());
            incident.setLatitude(s.latitude());
            incident.setLongitude(s.longitude());
            if (incident.getSecteurId() == null) {
                incident.setSecteurId(s.secteurId());
            }
        } catch (RuntimeException e) {
            log.warn("Signalement indisponible, position du feu inconnue pour le signalement {}",
                    incident.getSignalementId());
        }
    }

    /**
     * Appel synchrone à ms-ressources.
     * 409 de Ressources : aucun moyen libre (Ressources publie MoyensInsuffisants).
     * Ressources injoignable : l'incident passe en MOYENS_A_AFFECTER et la réservation est à relancer.
     */
    private List<MoyenEngage> reserver(Incident incident, TypeMoyen type, int nombre) {
        if (incident.getLatitude() == null || incident.getLongitude() == null) {
            mettreEnAttente(incident, "Position du feu inconnue : moyens a affecter");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Position du feu inconnue");
        }
        ReservationResultat resultat;
        try {
            resultat = ressourcesClient.reserver(new ReservationRequest(
                    incident.getId(), type, nombre, incident.getLatitude(), incident.getLongitude()));
        } catch (FeignException.Conflict e) {
            mettreEnAttente(incident, "Aucun " + type + " disponible");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Aucun moyen disponible");
        } catch (RuntimeException e) {
            mettreEnAttente(incident, "Ressources indisponible : reservation a relancer");
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Ressources indisponible, incident en MOYENS_A_AFFECTER");
        }

        List<MoyenEngage> engages = new ArrayList<>();
        if (resultat != null && resultat.reservations() != null) {
            for (RessourcesClient.Reservation r : resultat.reservations()) {
                if (!moyenRepository.existsByIncidentIdAndRessourceId(incident.getId(), r.ressourceId())) {
                    engages.add(engager(incident, r));
                }
            }
        }
        if (incident.getStatut() == StatutIncident.OUVERT || incident.getStatut() == StatutIncident.MOYENS_A_AFFECTER) {
            incident.setStatut(StatutIncident.EN_COURS);
            incidentRepository.save(incident);
        }
        tracer(incident, engages.size() + " moyen(s) " + type + " engage(s)", SourceAction.SYSTEME);
        return engages;
    }

    private MoyenEngage engager(Incident incident, RessourcesClient.Reservation reservation) {
        MoyenEngage moyen = new MoyenEngage();
        moyen.setIncident(incident);
        moyen.setRessourceId(reservation.ressourceId());
        moyen.setReservationId(reservation.id());
        moyen.setType(reservation.type());
        moyen.setEngageLe(LocalDateTime.now());
        return moyenRepository.save(moyen);
    }

    /** Un incident sans moyens passe en MOYENS_A_AFFECTER ; un incident déjà EN_COURS garde son statut. */
    private void mettreEnAttente(Incident incident, String motif) {
        if (incident.getStatut() == StatutIncident.OUVERT) {
            incident.setStatut(StatutIncident.MOYENS_A_AFFECTER);
            incidentRepository.save(incident);
        }
        tracer(incident, motif, SourceAction.SYSTEME);
    }
}
