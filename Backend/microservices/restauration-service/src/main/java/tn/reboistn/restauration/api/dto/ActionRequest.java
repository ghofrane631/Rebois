package tn.reboistn.restauration.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tn.reboistn.restauration.domain.TypeAction;

public record ActionRequest(
        @NotNull TypeAction type,
        @NotBlank @Size(max = 500) String description
) {
}