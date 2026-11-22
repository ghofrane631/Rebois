package tn.reboistn.restauration.api.dto;

import tn.reboistn.restauration.domain.ActionRestauration;
import tn.reboistn.restauration.domain.EvaluationDegats;
import tn.reboistn.restauration.domain.PlanRestauration;
import tn.reboistn.restauration.domain.Gravite;
import tn.reboistn.restauration.domain.EtatSol;
import tn.reboistn.restauration.domain.StatutZone;
import tn.reboistn.restauration.domain.SuiviRepousse;
import tn.reboistn.restauration.domain.TypeAction;
import tn.reboistn.restauration.domain.ZoneSinistree;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ZoneResponse(
        Long id,
        Long incidentId,
        Long secteurId,
        BigDecimal surfaceBruleeHa,
        String gouvernorat,
        Gravite gravite,
        StatutZone statut,
        EvaluationResponse evaluation,
        PlanResponse plan,
        List<SuiviResponse> suivis
) {
    public static ZoneResponse from(ZoneSinistree zone) {
        EvaluationResponse evaluation = zone.getEvaluation() == null
                ? null : EvaluationResponse.from(zone.getEvaluation());
        PlanResponse plan = zone.getPlan() == null ? null : PlanResponse.from(zone.getPlan());
        List<SuiviResponse> suivis = zone.getSuivis().stream().map(SuiviResponse::from).toList();
        return new ZoneResponse(zone.getId(), zone.getIncidentId(), zone.getSecteurId(),
                zone.getSurfaceBruleeHa(), zone.getGouvernorat(), zone.getGravite(), zone.getStatut(),
                evaluation, plan, suivis);
    }

    public record EvaluationResponse(int arbresPerdus, EtatSol etatSol, boolean risqueErosion,
                                     Gravite gravite, LocalDateTime evalueeLe) {
        private static EvaluationResponse from(EvaluationDegats evaluation) {
            return new EvaluationResponse(evaluation.getArbresPerdus(), evaluation.getEtatSol(),
                    evaluation.isRisqueErosion(), evaluation.getGravite(), evaluation.getEvalueeLe());
        }
    }

    public record PlanResponse(String vegetationCible, BigDecimal budgetEstime, List<ActionResponse> actions) {
        private static PlanResponse from(PlanRestauration plan) {
            return new PlanResponse(plan.getVegetationCible(), plan.getBudgetEstime(),
                    plan.getActions().stream().map(ActionResponse::from).toList());
        }
    }

    public record ActionResponse(Long id, TypeAction type, String description, boolean realisee) {
        private static ActionResponse from(ActionRestauration action) {
            return new ActionResponse(action.getId(), action.getType(), action.getDescription(), action.isRealisee());
        }
    }

    public record SuiviResponse(Long id, LocalDate date, int couverturePct, String gardeId) {
        private static SuiviResponse from(SuiviRepousse suivi) {
            return new SuiviResponse(suivi.getId(), suivi.getDateSuivi(), suivi.getCouverturePct(), suivi.getGardeId());
        }
    }
}