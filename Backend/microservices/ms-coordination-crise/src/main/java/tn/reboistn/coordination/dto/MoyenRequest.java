package tn.reboistn.coordination.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tn.reboistn.coordination.model.TypeMoyen;

/** Corps de POST /api/incidents/{id}/moyens : {"type":"CAMION_CITERNE","nombre":2}. */
public record MoyenRequest(@NotNull TypeMoyen type, @NotNull @Min(1) @Max(10) Integer nombre) {
}
