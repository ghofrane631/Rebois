package com.esprit.ffw.signalement.dto;
import jakarta.validation.constraints.NotBlank;
public record PhotoRequest(@NotBlank String url) {}
