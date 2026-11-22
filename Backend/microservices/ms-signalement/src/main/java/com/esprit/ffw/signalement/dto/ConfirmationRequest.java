package com.esprit.ffw.signalement.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ConfirmationRequest", description = "Motif facultatif de la décision du garde")
public record ConfirmationRequest(
        @Schema(description = "Motif de confirmation ou de fausse alerte",
                example = "Fumée confirmée sur place")
        String motif) {}
