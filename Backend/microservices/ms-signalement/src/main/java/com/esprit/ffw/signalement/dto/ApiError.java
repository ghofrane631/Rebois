package com.esprit.ffw.signalement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/** Corps d'erreur normalisé produit par GlobalExceptionHandler (documentation OpenAPI). */
@Schema(name = "ApiError", description = "Erreur normalisée : status HTTP, code métier et message")
public record ApiError(
        @Schema(description = "Code HTTP", example = "404") int status,
        @Schema(description = "Code métier stable", example = "SIGNALEMENT_INTROUVABLE") String code,
        @Schema(description = "Message lisible", example = "Signalement introuvable : 66f1c2a8e4b0a1f2c3d4e5f6")
        String message) {
}
