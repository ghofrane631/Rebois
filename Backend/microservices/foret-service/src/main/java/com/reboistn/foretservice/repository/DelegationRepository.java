package com.reboistn.foretservice.repository;

import com.reboistn.foretservice.entity.Delegation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DelegationRepository extends JpaRepository<Delegation, Long> {
    List<Delegation> findByRegionId(Long regionId);
    boolean existsByNomAndRegionId(String nom, Long regionId);
}
