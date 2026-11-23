package com.reboistn.foretservice.service.impl;

import com.reboistn.foretservice.dto.request.ZoneForestiereRequest;
import com.reboistn.foretservice.dto.response.ZoneForestiereResponse;
import com.reboistn.foretservice.entity.Delegation;
import com.reboistn.foretservice.entity.StatutZone;
import com.reboistn.foretservice.entity.TypeZone;
import com.reboistn.foretservice.entity.ZoneForestiere;
import com.reboistn.foretservice.exception.DuplicateResourceException;
import com.reboistn.foretservice.exception.ResourceNotFoundException;
import com.reboistn.foretservice.mapper.ZoneForestiereMapper;
import com.reboistn.foretservice.repository.DelegationRepository;
import com.reboistn.foretservice.repository.ZoneForestiereRepository;
import com.reboistn.foretservice.service.ZoneForestiereService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ZoneForestiereServiceImpl implements ZoneForestiereService {

    private final ZoneForestiereRepository zoneRepository;
    private final DelegationRepository delegationRepository;
    private final ZoneForestiereMapper mapper;

    @Override
    @Transactional
    public ZoneForestiereResponse create(ZoneForestiereRequest request) {
        if (zoneRepository.existsByNom(request.getNom())) {
            throw new DuplicateResourceException("Une zone avec le nom '" + request.getNom() + "' existe déjà");
        }
        Delegation delegation = delegationRepository.findById(request.getDelegationId())
                .orElseThrow(() -> new ResourceNotFoundException("Délégation", request.getDelegationId()));
        ZoneForestiere zone = mapper.toEntity(request, delegation);
        return mapper.toResponse(zoneRepository.save(zone));
    }

    @Override
    @Transactional
    public ZoneForestiereResponse update(Long id, ZoneForestiereRequest request) {
        ZoneForestiere zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone forestière", id));
        if (zoneRepository.existsByNomAndIdNot(request.getNom(), id)) {
            throw new DuplicateResourceException("Une zone avec le nom '" + request.getNom() + "' existe déjà");
        }
        Delegation delegation = delegationRepository.findById(request.getDelegationId())
                .orElseThrow(() -> new ResourceNotFoundException("Délégation", request.getDelegationId()));
        mapper.updateEntity(zone, request, delegation);
        return mapper.toResponse(zoneRepository.save(zone));
    }

    @Override
    public ZoneForestiereResponse findById(Long id) {
        ZoneForestiere zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone forestière", id));
        return mapper.toResponse(zone);
    }

    @Override
    public Page<ZoneForestiereResponse> findAll(TypeZone type, StatutZone statut, Long delegationId, Long regionId, Pageable pageable) {
        // Par défaut, on exclut les archivées si aucun statut n'est précisé
        StatutZone filtreStatut = (statut == null) ? StatutZone.ACTIVE : statut;
        return zoneRepository
                .findAllFiltered(type, filtreStatut, delegationId, regionId, pageable)
                .map(mapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ZoneForestiere zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone forestière", id));
        // Archivage logique
        zone.setStatut(StatutZone.ARCHIVEE);
        zoneRepository.save(zone);
    }

    @Override
    @Transactional
    public ZoneForestiereResponse archive(Long id) {
        ZoneForestiere zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone forestière", id));
        zone.setStatut(StatutZone.ARCHIVEE);
        return mapper.toResponse(zoneRepository.save(zone));
    }
}
