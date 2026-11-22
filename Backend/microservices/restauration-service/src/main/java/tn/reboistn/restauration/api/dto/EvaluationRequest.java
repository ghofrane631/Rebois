package tn.reboistn.restauration.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tn.reboistn.restauration.domain.EtatSol;
import tn.reboistn.restauration.domain.Gravite;

public record EvaluationRequest(
        @Min(0) int arbresPerdus,
        @NotNull EtatSol etatSol,
        boolean risqueErosion,
        Gravite gravite
) {
}