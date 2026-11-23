package com.reboistn.foretservice.service.impl;

import com.reboistn.foretservice.dto.request.DelegationRequest;
import com.reboistn.foretservice.dto.response.DelegationResponse;
import com.reboistn.foretservice.entity.Delegation;
import com.reboistn.foretservice.entity.Region;
import com.reboistn.foretservice.exception.DuplicateResourceException;
import com.reboistn.foretservice.exception.ResourceNotFoundException;
import com.reboistn.foretservice.mapper.DelegationMapper;
import com.reboistn.foretservice.repository.DelegationRepository;
import com.reboistn.foretservice.repository.RegionRepository;
import com.reboistn.foretservice.service.DelegationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DelegationServiceImpl implements DelegationService {

    private final DelegationRepository delegationRepository;
    private final RegionRepository regionRepository;
    private final DelegationMapper delegationMapper;

    @Override
    public List<DelegationResponse> findByRegionId(Long regionId) {
        if (!regionRepository.existsById(regionId)) {
            throw new ResourceNotFoundException("Région", regionId);
        }
        return delegationRepository.findByRegionId(regionId)
                .stream()
                .map(delegationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DelegationResponse findById(Long id) {
        Delegation delegation = delegationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Délégation", id));
        return delegationMapper.toResponse(delegation);
    }

    @Override
    @Transactional
    public DelegationResponse create(DelegationRequest request) {
        Region region = regionRepository.findById(request.getRegionId())
                .orElseThrow(() -> new ResourceNotFoundException("Région", request.getRegionId()));
        if (delegationRepository.existsByNomAndRegionId(request.getNom(), request.getRegionId())) {
            throw new DuplicateResourceException(
                    "Une délégation '" + request.getNom() + "' existe déjà dans cette région");
        }
        Delegation saved = delegationRepository.save(delegationMapper.toEntity(request, region));
        return delegationMapper.toResponse(saved);
    }
}
