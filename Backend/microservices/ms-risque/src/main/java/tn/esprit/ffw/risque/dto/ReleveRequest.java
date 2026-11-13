package tn.esprit.ffw.risque.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record ReleveRequest(
        @NotNull Long secteurId,
        @NotNull @DecimalMin("-30.0") @DecimalMax("60.0") Double temperature,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double humidite,
        @NotNull @DecimalMin("0.0") @DecimalMax("300.0") Double vent,
        @NotNull @Min(0) Integer joursSansPluie,
        LocalDateTime dateReleve) { }
