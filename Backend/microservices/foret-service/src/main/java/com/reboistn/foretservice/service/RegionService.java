package com.reboistn.foretservice.service;

import com.reboistn.foretservice.dto.request.RegionRequest;
import com.reboistn.foretservice.dto.response.RegionResponse;

import java.util.List;

public interface RegionService {
    List<RegionResponse> findAll();
    RegionResponse findById(Long id);
    RegionResponse create(RegionRequest request);
}
