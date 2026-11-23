package com.reboistn.foretservice.dto.response;

import com.reboistn.foretservice.entity.StatutZone;
import com.reboistn.foretservice.entity.TypeZone;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ZoneForestiereResponse {
    private Long id;
    private String nom;
    private TypeZone type;
    private Double superficieHa;
    private String description;
    private String geometrieWkt;
    private StatutZone statut;
    private LocalDateTime dateCreation;
    private LocalDateTime dateModification;
    private Long delegationId;
    private String delegationNom;
    private Long regionId;
    private String regionNom;
}
