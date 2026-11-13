package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.model.EvenementChrono;

import java.util.List;

public interface EvenementChronoRepository extends JpaRepository<EvenementChrono, Long> {

    List<EvenementChrono> findByIncidentIdOrderByHeureAsc(Long incidentId);
}
