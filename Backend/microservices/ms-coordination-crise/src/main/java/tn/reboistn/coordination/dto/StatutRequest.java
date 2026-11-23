package tn.reboistn.coordination.dto;

import jakarta.validation.constraints.NotNull;
import tn.reboistn.coordination.entity.StatutIncident;

/** Corps de PATCH /api/incidents/{id}/statut. */
public record StatutRequest(@NotNull StatutIncident statut) {
}
