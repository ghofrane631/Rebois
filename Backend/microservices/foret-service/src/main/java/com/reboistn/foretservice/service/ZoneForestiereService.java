package com.reboistn.foretservice.service;

import com.reboistn.foretservice.dto.request.ZoneForestiereRequest;
import com.reboistn.foretservice.dto.response.ZoneForestiereResponse;
import com.reboistn.foretservice.entity.StatutZone;
import com.reboistn.foretservice.entity.TypeZone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ZoneForestiereService {
    ZoneForestiereResponse create(ZoneForestiereRequest request);
    ZoneForestiereResponse update(Long id, ZoneForestiereRequest request);
    ZoneForestiereResponse findById(Long id);
    Page<ZoneForestiereResponse> findAll(TypeZone type, StatutZone statut, Long delegationId, Long regionId, Pageable pageable);
    void delete(Long id);
    ZoneForestiereResponse archive(Long id);
}
