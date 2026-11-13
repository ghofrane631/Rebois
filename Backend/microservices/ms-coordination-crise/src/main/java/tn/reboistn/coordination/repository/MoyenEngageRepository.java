package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.model.MoyenEngage;

import java.util.List;

public interface MoyenEngageRepository extends JpaRepository<MoyenEngage, Long> {

    List<MoyenEngage> findByIncidentId(Long incidentId);

    boolean existsByIncidentIdAndRessourceId(Long incidentId, Long ressourceId);
}
