package com.esprit.ffw.signalement.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "PhotoRequest", description = "URL d'une photo à rattacher au signalement")
public record PhotoRequest(
        @Schema(description = "URL http(s) de l'image", example = "https://cdn.reboistn.tn/signalements/p2.jpg",
                maxLength = 2048, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank @Size(max = 2048)
        @Pattern(regexp = "^https?://.+", message = "URL http(s) attendue") String url) {}
