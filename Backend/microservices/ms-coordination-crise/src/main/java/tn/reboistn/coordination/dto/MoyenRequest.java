package tn.reboistn.coordination.dto;

import jakarta.validation.constraints.NotNull;

/** Corps de POST /api/incidents/{id}/moyens. */
public record MoyenRequest(@NotNull Long ressourceId, Long reservationId) {
}
