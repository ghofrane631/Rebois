package com.reboistn.foretservice.repository;

import com.reboistn.foretservice.entity.StatutZone;
import com.reboistn.foretservice.entity.TypeZone;
import com.reboistn.foretservice.entity.ZoneForestiere;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ZoneForestiereRepository extends JpaRepository<ZoneForestiere, Long>, JpaSpecificationExecutor<ZoneForestiere> {

    boolean existsByNom(String nom);
    boolean existsByNomAndIdNot(String nom, Long id);

    Optional<ZoneForestiere> findByIdAndStatut(Long id, StatutZone statut);

    @Query("""
        SELECT z FROM ZoneForestiere z
        WHERE (:type IS NULL OR z.type = :type)
          AND (:statut IS NULL OR z.statut = :statut)
          AND (:delegationId IS NULL OR z.delegation.id = :delegationId)
          AND (:regionId IS NULL OR z.delegation.region.id = :regionId)
    """)
    Page<ZoneForestiere> findAllFiltered(
            @Param("type") TypeZone type,
            @Param("statut") StatutZone statut,
            @Param("delegationId") Long delegationId,
            @Param("regionId") Long regionId,
            Pageable pageable
    );
}
