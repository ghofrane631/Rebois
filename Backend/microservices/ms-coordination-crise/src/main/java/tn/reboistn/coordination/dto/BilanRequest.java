package tn.reboistn.coordination.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/** Corps de PUT /api/incidents/{id}/bilan. */
public record BilanRequest(
        @NotNull @PositiveOrZero Double surfaceBruleeHa,
        @NotNull @PositiveOrZero Integer dureeMin,
        String cause) {
}
