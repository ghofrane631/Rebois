package com.reboistn.foretservice.mapper;

import com.reboistn.foretservice.dto.request.RegionRequest;
import com.reboistn.foretservice.dto.response.RegionResponse;
import com.reboistn.foretservice.entity.Region;
import org.springframework.stereotype.Component;

@Component
public class RegionMapper {

    public Region toEntity(RegionRequest request) {
        return Region.builder()
                .nom(request.getNom())
                .gouvernorat(request.getGouvernorat())
                .build();
    }

    public RegionResponse toResponse(Region region) {
        return RegionResponse.builder()
                .id(region.getId())
                .nom(region.getNom())
                .gouvernorat(region.getGouvernorat())
                .build();
    }
}
