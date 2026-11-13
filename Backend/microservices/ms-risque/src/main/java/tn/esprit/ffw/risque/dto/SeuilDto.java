package tn.esprit.ffw.risque.dto;

import jakarta.validation.constraints.*;
import tn.esprit.ffw.risque.entity.NiveauRisque;

public record SeuilDto(@NotNull NiveauRisque niveau, @Min(0) @Max(100) int valeurMin) { }
