package com.reboistn.foretservice.dto.request;

import com.reboistn.foretservice.entity.TypeZone;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ZoneForestiereRequest {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotNull(message = "Le type est obligatoire")
    private TypeZone type;

    @NotNull(message = "La superficie est obligatoire")
    @DecimalMin(value = "0.01", message = "La superficie doit être supérieure à 0")
    private Double superficieHa;

    private String description;

    /**
     * Géométrie WKT (Polygon) optionnelle, ex: "POLYGON((9.5 36.8, 9.6 36.8, 9.6 36.9, 9.5 36.9, 9.5 36.8))"
     */
    private String geometrieWkt;

    @NotNull(message = "La délégation est obligatoire")
    private Long delegationId;
}
