package com.esprit.ffw.signalement.dto;

import com.esprit.ffw.signalement.model.TypeSignalement;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(name = "SignalementRequest", description = "Données de déclaration d'un départ de feu")
public record SignalementRequest(
        @Schema(description = "Type observé sur place", example = "FUMEE",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull TypeSignalement type,

        @Schema(description = "Latitude WGS84", example = "36.8065",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,

        @Schema(description = "Longitude WGS84", example = "10.1815",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude,

        @Schema(description = "Intensité perçue, de 1 (faible) à 5 (forte)", example = "3",
                minimum = "1", maximum = "5", requiredMode = Schema.RequiredMode.REQUIRED)
        @Min(1) @Max(5) int intensite,

        @Schema(description = "Description libre de la situation", example = "Fumée épaisse au-dessus de la pinède",
                maxLength = 500)
        @Size(max = 500) String description,

        @Schema(description = "URL http(s) d'une première photo", example = "https://cdn.reboistn.tn/signalements/p1.jpg",
                maxLength = 2048)
        @Size(max = 2048) @Pattern(regexp = "^https?://.+", message = "URL http(s) attendue") String photoUrl) {}
