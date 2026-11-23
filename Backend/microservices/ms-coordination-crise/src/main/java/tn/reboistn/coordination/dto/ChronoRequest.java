package tn.reboistn.coordination.dto;

import jakarta.validation.constraints.NotBlank;

/** Corps de POST /api/incidents/{id}/chronologie. */
public record ChronoRequest(@NotBlank String action) {
}
