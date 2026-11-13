package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.model.Incident;
import tn.reboistn.coordination.model.StatutIncident;

import java.util.List;

public interface IncidentRepository extends JpaRepository<Incident, Long> {

    List<Incident> findByStatut(StatutIncident statut);

    boolean existsBySignalementId(Long signalementId);
}
