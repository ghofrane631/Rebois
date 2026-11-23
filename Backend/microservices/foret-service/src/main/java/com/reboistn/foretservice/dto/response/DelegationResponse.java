package com.reboistn.foretservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DelegationResponse {
    private Long id;
    private String nom;
    private Long regionId;
    private String regionNom;
}
