package com.esprit.ffw.signalement.dto;

import com.esprit.ffw.signalement.model.TypeSignalement;
import jakarta.validation.constraints.*;

public record SignalementRequest(
        @NotNull TypeSignalement type,
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,
        @Min(1) @Max(5) int intensite,
        @Size(max = 500) String description,
        String photoUrl) {}
