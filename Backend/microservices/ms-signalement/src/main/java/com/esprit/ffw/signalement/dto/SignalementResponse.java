package com.esprit.ffw.signalement.dto;

import com.esprit.ffw.signalement.model.Signalement;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Le signalement créé + les ids des signalements proches (doublons possibles). */
@Schema(name = "SignalementResponse", description = "Signalement créé et doublons proches détectés")
public record SignalementResponse(
        @Schema(description = "Signalement persisté") Signalement signalement,
        @Schema(description = "Identifiants des signalements actifs proches (doublons potentiels)",
                example = "[\"66f1c2a8e4b0a1f2c3d4e5f6\"]")
        List<String> doublonsProches) {}
