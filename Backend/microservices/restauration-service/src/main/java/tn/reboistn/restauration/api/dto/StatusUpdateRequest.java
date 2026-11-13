package tn.reboistn.restauration.api.dto;

import jakarta.validation.constraints.NotNull;
import tn.reboistn.restauration.domain.StatutZone;

public record StatusUpdateRequest(@NotNull StatutZone statut) {
}