package com.reboistn.foretservice.repository;

import com.reboistn.foretservice.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
    Optional<Region> findByNom(String nom);
    boolean existsByNom(String nom);
}
