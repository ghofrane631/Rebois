package com.esprit.ffw.signalement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data @NoArgsConstructor @AllArgsConstructor
@Schema(name = "Photo", description = "Photo associée à un signalement")
public class Photo {
    @Schema(description = "URL http(s) de l'image", example = "https://cdn.reboistn.tn/signalements/p1.jpg")
    private String url;

    @Schema(description = "Date d'ajout (UTC)", example = "2026-11-16T18:43:00Z")
    private Instant priseLe;
}
