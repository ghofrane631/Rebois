package com.reboistn.foretservice.service;

import com.reboistn.foretservice.dto.request.DelegationRequest;
import com.reboistn.foretservice.dto.response.DelegationResponse;

import java.util.List;

public interface DelegationService {
    List<DelegationResponse> findByRegionId(Long regionId);
    DelegationResponse findById(Long id);
    DelegationResponse create(DelegationRequest request);
}
