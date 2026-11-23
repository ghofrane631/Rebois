package com.reboistn.foretservice.service.impl;

import com.reboistn.foretservice.dto.request.RegionRequest;
import com.reboistn.foretservice.dto.response.RegionResponse;
import com.reboistn.foretservice.entity.Region;
import com.reboistn.foretservice.exception.DuplicateResourceException;
import com.reboistn.foretservice.exception.ResourceNotFoundException;
import com.reboistn.foretservice.mapper.RegionMapper;
import com.reboistn.foretservice.repository.RegionRepository;
import com.reboistn.foretservice.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    @Override
    public List<RegionResponse> findAll() {
        return regionRepository.findAll()
                .stream()
                .map(regionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RegionResponse findById(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Région", id));
        return regionMapper.toResponse(region);
    }

    @Override
    @Transactional
    public RegionResponse create(RegionRequest request) {
        if (regionRepository.existsByNom(request.getNom())) {
            throw new DuplicateResourceException("Une région avec le nom '" + request.getNom() + "' existe déjà");
        }
        Region saved = regionRepository.save(regionMapper.toEntity(request));
        return regionMapper.toResponse(saved);
    }
}
