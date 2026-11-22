package com.esprit.ffw.signalement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(name = "HistoriqueStatut", description = "Une transition de statut d'un signalement")
public class HistoriqueStatut {
    @Schema(description = "Statut avant la transition", example = "SIGNALE")
    private Statut ancien;

    @Schema(description = "Statut après la transition", example = "CONFIRME")
    private Statut nouveau;

    @Schema(description = "Subject Keycloak du garde ayant effectué la transition", example = "garde-4")
    private String gardeId;

    @Schema(description = "Motif éventuel", example = "Fumée confirmée sur place")
    private String motif;

    @Schema(description = "Date de la transition (UTC)", example = "2026-11-16T18:45:00Z")
    private Instant date;
}
