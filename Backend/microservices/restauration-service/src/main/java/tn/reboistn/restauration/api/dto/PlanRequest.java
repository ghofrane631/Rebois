package tn.reboistn.restauration.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.util.List;

public record PlanRequest(
        @DecimalMin(value = "0.0") BigDecimal budgetEstime,
        List<@Valid ActionRequest> actions
) {
}