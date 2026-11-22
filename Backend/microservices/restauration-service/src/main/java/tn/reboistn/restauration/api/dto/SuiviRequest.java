package tn.reboistn.restauration.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record SuiviRequest(
        @NotNull LocalDate date,
        @Min(0) @Max(100) int couverturePct,
        @NotBlank String gardeId
) {
}