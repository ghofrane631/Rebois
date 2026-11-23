package com.reboistn.foretservice.mapper;

import com.reboistn.foretservice.dto.request.DelegationRequest;
import com.reboistn.foretservice.dto.response.DelegationResponse;
import com.reboistn.foretservice.entity.Delegation;
import com.reboistn.foretservice.entity.Region;
import org.springframework.stereotype.Component;

@Component
public class DelegationMapper {

    public Delegation toEntity(DelegationRequest request, Region region) {
        return Delegation.builder()
                .nom(request.getNom())
                .region(region)
                .build();
    }

    public DelegationResponse toResponse(Delegation delegation) {
        return DelegationResponse.builder()
                .id(delegation.getId())
                .nom(delegation.getNom())
                .regionId(delegation.getRegion().getId())
                .regionNom(delegation.getRegion().getNom())
                .build();
    }
}
