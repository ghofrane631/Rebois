package com.reboistn.foretservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegionResponse {
    private Long id;
    private String nom;
    private String gouvernorat;
}
