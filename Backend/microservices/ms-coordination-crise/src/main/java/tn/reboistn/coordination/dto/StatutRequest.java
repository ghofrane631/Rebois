package tn.reboistn.coordination.dto;

import jakarta.validation.constraints.NotNull;
import tn.reboistn.coordination.model.StatutIncident;

/** Corps de PATCH /api/incidents/{id}/statut. */
public record StatutRequest(@NotNull StatutIncident statut) {
}
