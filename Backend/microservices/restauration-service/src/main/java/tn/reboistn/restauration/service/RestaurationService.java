package tn.reboistn.restauration.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.reboistn.restauration.api.dto.ZoneResponse;
import tn.reboistn.restauration.api.dto.ActionRequest;
import tn.reboistn.restauration.api.dto.EvaluationRequest;
import tn.reboistn.restauration.api.dto.PlanRequest;
import tn.reboistn.restauration.api.dto.SuiviRequest;
import tn.reboistn.restauration.client.ForetClient;
import tn.reboistn.restauration.domain.ActionRestauration;
import tn.reboistn.restauration.domain.EvaluationDegats;
import tn.reboistn.restauration.domain.EvenementTraite;
import tn.reboistn.restauration.domain.PlanRestauration;
import tn.reboistn.restauration.domain.StatutZone;
import tn.reboistn.restauration.domain.SuiviRepousse;
import tn.reboistn.restauration.domain.ZoneSinistree;
import tn.reboistn.restauration.event.InterventionTermineeEvent;
import tn.reboistn.restauration.event.ZoneEventPublisher;
import tn.reboistn.restauration.event.ZoneRestaureeEvent;
import tn.reboistn.restauration.exception.BadRequestException;
import tn.reboistn.restauration.exception.ConflictException;
import tn.reboistn.restauration.exception.NotFoundException;
import tn.reboistn.restauration.repository.ActionRestaurationRepository;
import tn.reboistn.restauration.repository.EvenementTraiteRepository;
import tn.reboistn.restauration.repository.ZoneSinistreeRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class RestaurationService {

    private static final int COUVERTURE_RESTAURATION_PCT = 70;

    private final ZoneSinistreeRepository zoneRepository;
    private final ActionRestaurationRepository actionRepository;
    private final EvenementTraiteRepository evenementRepository;
    private final ForetClient foretClient;
    private final ZoneEventPublisher eventPublisher;

    public RestaurationService(ZoneSinistreeRepository zoneRepository,
                               ActionRestaurationRepository actionRepository,
                               EvenementTraiteRepository evenementRepository,
                               ForetClient foretClient,
                               ZoneEventPublisher eventPublisher) {
        this.zoneRepository = zoneRepository;
        this.actionRepository = actionRepository;
        this.evenementRepository = evenementRepository;
        this.foretClient = foretClient;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<ZoneResponse> findZones(StatutZone statut, String gouvernorat) {
        boolean hasGovernorate = gouvernorat != null && !gouvernorat.isBlank();
        List<ZoneSinistree> zones;
        if (statut != null && hasGovernorate) {
            zones = zoneRepository.findAllByStatutAndGouvernoratIgnoreCase(statut, gouvernorat.trim());
        } else if (statut != null) {
            zones = zoneRepository.findAllByStatut(statut);
        } else if (hasGovernorate) {
            zones = zoneRepository.findAllByGouvernoratIgnoreCase(gouvernorat.trim());
        } else {
            zones = zoneRepository.findAll();
        }
        return zones.stream().map(ZoneResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public ZoneResponse getZone(Long zoneId) {
        return ZoneResponse.from(findZone(zoneId));
    }

    @Transactional
    public ZoneResponse evaluate(Long zoneId, EvaluationRequest request) {
        ZoneSinistree zone = findZone(zoneId);
        if (zone.getEvaluation() != null) {
            throw new ConflictException("ZONE_DEJA_EVALUEE", "La zone possede deja une evaluation");
        }
        if (zone.getStatut() != StatutZone.A_EVALUER) {
            throw new ConflictException("TRANSITION_INTERDITE", "La zone ne peut plus etre evaluee");
        }
        zone.setEvaluation(new EvaluationDegats(request.arbresPerdus(), request.etatSol(),
                request.risqueErosion(), request.gravite()));
        return ZoneResponse.from(zoneRepository.save(zone));
    }

    @Transactional
    public ZoneResponse createPlan(Long zoneId, PlanRequest request) {
        ZoneSinistree zone = findZone(zoneId);
        if (zone.getEvaluation() == null) {
            throw new ConflictException("EVALUATION_REQUISE", "Une evaluation des degats est requise avant le plan");
        }
        if (zone.getPlan() != null) {
            throw new ConflictException("PLAN_DEJA_CREE", "Un plan existe deja pour cette zone");
        }
        if (request.budgetEstime() == null) {
            throw new BadRequestException("BUDGET_REQUIS", "Le budget estime est obligatoire");
        }

        String vegetationCible = null;
        try {
            ForetClient.VegetationResponse vegetation = foretClient.getVegetationCible(zone.getSecteurId());
            if (vegetation != null) {
                vegetationCible = vegetation.vegetationCible();
            }
        } catch (RuntimeException ignored) {
            vegetationCible = null;
        }

        PlanRestauration plan = new PlanRestauration(vegetationCible, request.budgetEstime());
        if (request.actions() != null) {
            for (ActionRequest action : request.actions()) {
                plan.addAction(new ActionRestauration(action.type(), action.description()));
            }
        }
        zone.setPlan(plan);
        return ZoneResponse.from(zoneRepository.save(zone));
    }

    @Transactional
    public ActionRestauration updateAction(Long actionId, boolean realisee) {
        ActionRestauration action = actionRepository.findById(actionId)
                .orElseThrow(() -> new NotFoundException("Action"));
        StatutZone statut = action.getPlan().getZone().getStatut();
        if (statut != StatutZone.EN_RESTAURATION) {
            throw new ConflictException("TRANSITION_INTERDITE", "Les actions ne peuvent etre modifiees que pendant la restauration");
        }
        action.setRealisee(realisee);
        return actionRepository.save(action);
    }

    @Transactional
    public ZoneResponse addSuivi(Long zoneId, SuiviRequest request) {
        ZoneSinistree zone = findZone(zoneId);
        if (zone.getPlan() == null || zone.getStatut() != StatutZone.EN_RESTAURATION) {
            throw new ConflictException("PLAN_REQUIS", "La zone doit etre en restauration pour recevoir un suivi");
        }
        if (!zone.getSuivis().isEmpty() && request.date().isBefore(zone.getSuivis().get(0).getDateSuivi())) {
            throw new BadRequestException("DATE_SUIVI_ANTERIEURE", "La date du suivi doit suivre le dernier releve");
        }

        SuiviRepousse suivi = new SuiviRepousse(request.date(), request.couverturePct(), request.gardeId());
        zone.addSuivi(suivi);
        if (request.couverturePct() >= COUVERTURE_RESTAURATION_PCT) {
            zone.setStatut(StatutZone.RESTAUREE);
        }
        ZoneSinistree saved = zoneRepository.save(zone);
        if (saved.getStatut() == StatutZone.RESTAUREE) {
            eventPublisher.publish(new ZoneRestaureeEvent(UUID.randomUUID().toString(), saved.getId(),
                    saved.getSecteurId(), request.couverturePct(), LocalDateTime.now()));
        }
        return ZoneResponse.from(saved);
    }

    @Transactional
    public ZoneResponse updateStatus(Long zoneId, StatutZone newStatus) {
        ZoneSinistree zone = findZone(zoneId);
        if (newStatus == zone.getStatut()) {
            throw new ConflictException("TRANSITION_INTERDITE", "La zone possede deja ce statut");
        }
        boolean allowed = switch (newStatus) {
            case EVALUEE -> zone.getEvaluation() != null;
            case EN_RESTAURATION -> zone.getPlan() != null;
            case RESTAUREE -> latestCoverage(zone) >= COUVERTURE_RESTAURATION_PCT;
            case A_EVALUER -> false;
        };
        if (!allowed || newStatus.ordinal() < zone.getStatut().ordinal()) {
            throw new ConflictException("TRANSITION_INTERDITE", "Transition de statut impossible");
        }
        zone.setStatut(newStatus);
        ZoneSinistree saved = zoneRepository.save(zone);
        if (newStatus == StatutZone.RESTAUREE) {
            eventPublisher.publish(new ZoneRestaureeEvent(UUID.randomUUID().toString(), saved.getId(),
                saved.getSecteurId(), latestCoverage(saved), LocalDateTime.now()));
        }
        return ZoneResponse.from(saved);
    }

    @Transactional
    public void createZoneFromIntervention(InterventionTermineeEvent event) {
        if (event.eventId() == null || event.eventId().isBlank() || event.incidentId() == null
                || event.secteurId() == null || event.surfaceBruleeHa() == null
                || event.surfaceBruleeHa().compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("EVENEMENT_INVALIDE", "L'evenement InterventionTerminee est incomplet");
        }
        if (evenementRepository.existsById(event.eventId())) {
            return;
        }
        if (zoneRepository.findByIncidentId(event.incidentId()).isEmpty()) {
            zoneRepository.save(new ZoneSinistree(event.incidentId(), event.secteurId(),
                    event.surfaceBruleeHa(), event.gouvernorat()));
        }
        evenementRepository.save(new EvenementTraite(event.eventId()));
    }

    private int latestCoverage(ZoneSinistree zone) {
        return zone.getSuivis().isEmpty() ? 0 : zone.getSuivis().get(0).getCouverturePct();
    }

    private ZoneSinistree findZone(Long zoneId) {
        return zoneRepository.findById(zoneId).orElseThrow(() -> new NotFoundException("Zone sinistree"));
    }
}