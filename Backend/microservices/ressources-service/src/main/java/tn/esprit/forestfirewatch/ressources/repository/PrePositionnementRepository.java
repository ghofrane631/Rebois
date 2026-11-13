package tn.esprit.forestfirewatch.ressources.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forestfirewatch.ressources.entity.PrePositionnement;

import java.util.List;

public interface PrePositionnementRepository extends JpaRepository<PrePositionnement, Long> {

    List<PrePositionnement> findBySecteurId(Long secteurId);
}
