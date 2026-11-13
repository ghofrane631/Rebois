package tn.reboistn.coordination.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tn.reboistn.coordination.dto.*;
import tn.reboistn.coordination.model.*;
import tn.reboistn.coordination.repository.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/incidents")
@RequiredArgsConstructor
public class IncidentController {

    private final IncidentRepository incidentRepository;
    private final MoyenEngageRepository moyenRepository;
    private final EvenementChronoRepository chronoRepository;
    private final BilanRepository bilanRepository;
    private final AlerteEmiseRepository alerteRepository;
    private final AbonnementRepository abonnementRepository;

    @GetMapping
    public List<Incident> lister(@RequestParam(required = false) StatutIncident statut) {
        return statut == null ? incidentRepository.findAll() : incidentRepository.findByStatut(statut);
    }

    @GetMapping("/{id}")
    public IncidentDetail detail(@PathVariable Long id) {
        Incident incident = trouver(id);
        return new IncidentDetail(
                incident,
                chronoRepository.findByIncidentIdOrderByHeureAsc(id),
                moyenRepository.findByIncidentId(id),
                alerteRepository.findByIncident_Id(id),
                bilanRepository.findById(id).orElse(null));
    }

    /**
     * Ouvre un incident pour un feu confirmé et alerte les abonnés de la zone.
     * TODO S7 : appelé aussi à la réception de l'événement FeuConfirme (RabbitMQ).
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public Incident ouvrir(@Valid @RequestBody Incident incident) {
        if (incidentRepository.existsBySignalementId(incident.getSignalementId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Un incident existe deja pour ce signalement");
        }
        incident.setId(null);
        incident.setStatut(StatutIncident.OUVERT);
        incident.setOuvertLe(LocalDateTime.now());
        Incident cree = incidentRepository.save(incident);
        tracer(cree, "Incident ouvert pour le signalement " + cree.getSignalementId(), SourceAction.SYSTEME);
        alerterZone(cree);
        return cree;
    }

    /** TODO S6 : réserver le moyen auprès de ms-ressources (OpenFeign) avant de l'enregistrer. */
    @PostMapping("/{id}/moyens")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public MoyenEngage engagerMoyen(@PathVariable Long id, @Valid @RequestBody MoyenRequest request) {
        Incident incident = trouverNonClos(id);
        if (moyenRepository.existsByIncidentIdAndRessourceId(id, request.ressourceId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Moyen deja engage sur cet incident");
        }
        MoyenEngage moyen = new MoyenEngage();
        moyen.setIncident(incident);
        moyen.setRessourceId(request.ressourceId());
        moyen.setReservationId(request.reservationId());
        moyen.setEngageLe(LocalDateTime.now());
        MoyenEngage enregistre = moyenRepository.save(moyen);

        if (incident.getStatut() == StatutIncident.OUVERT || incident.getStatut() == StatutIncident.MOYENS_A_AFFECTER) {
            incident.setStatut(StatutIncident.EN_COURS);
        }
        tracer(incident, "Moyen " + request.ressourceId() + " engage", SourceAction.SYSTEME);
        return enregistre;
    }

    @PostMapping("/{id}/chronologie")
    @ResponseStatus(HttpStatus.CREATED)
    public EvenementChrono ajouterAction(@PathVariable Long id, @Valid @RequestBody ChronoRequest request) {
        return tracer(trouverNonClos(id), request.action(), SourceAction.AGENT);
    }

    /** La clôture (CLOS) ne passe que par le bilan : PUT /api/incidents/{id}/bilan. */
    @PatchMapping("/{id}/statut")
    @Transactional
    public Incident changerStatut(@PathVariable Long id, @Valid @RequestBody StatutRequest request) {
        Incident incident = trouverNonClos(id);
        if (request.statut() == StatutIncident.CLOS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La cloture se fait par le bilan");
        }
        incident.setStatut(request.statut());
        tracer(incident, "Statut passe a " + request.statut(), SourceAction.AGENT);
        return incident;
    }

    /** TODO S7 : publier l'événement InterventionTerminee après la clôture. */
    @PutMapping("/{id}/bilan")
    @Transactional
    public Bilan cloturer(@PathVariable Long id, @Valid @RequestBody BilanRequest request) {
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
        return enregistre;
    }

    @GetMapping("/{id}/alertes")
    public List<AlerteEmise> alertes(@PathVariable Long id) {
        trouver(id);
        return alerteRepository.findByIncident_Id(id);
    }

    // ---------- méthodes internes ----------

    private Incident trouver(Long id) {
        return incidentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Incident introuvable"));
    }

    private Incident trouverNonClos(Long id) {
        Incident incident = trouver(id);
        if (incident.getStatut() == StatutIncident.CLOS) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Incident deja clos");
        }
        return incident;
    }

    private EvenementChrono tracer(Incident incident, String action, SourceAction source) {
        EvenementChrono chrono = new EvenementChrono();
        chrono.setIncident(incident);
        chrono.setHeure(LocalDateTime.now());
        chrono.setAction(action);
        chrono.setSource(source);
        return chronoRepository.save(chrono);
    }

    /** Alerte chaque abonné de la zone dont le niveau minimum est atteint. En EXTREME, le SMS est obligatoire. */
    private void alerterZone(Incident incident) {
        Canal canal = incident.getNiveauRisque() == Niveau.EXTREME ? Canal.SMS : Canal.APP;
        abonnementRepository.findByGouvernorat(incident.getGouvernorat()).stream()
                .filter(a -> incident.getNiveauRisque().compareTo(a.getNiveauMin()) >= 0)
                .forEach(a -> {
                    AlerteEmise alerte = new AlerteEmise();
                    alerte.setIncident(incident);
                    alerte.setUserId(a.getUserId());
                    alerte.setType(TypeAlerte.INCIDENT);
                    alerte.setCanal(canal);
                    alerte.setEmiseLe(LocalDateTime.now());
                    alerteRepository.save(alerte);
                    if (canal == Canal.SMS) {
                        // envoi simulé : aucun fournisseur SMS n'est branché pour l'instant
                        log.info("SMS simule vers l'utilisateur {} pour l'incident {}", a.getUserId(), incident.getId());
                    }
                });
    }
}
