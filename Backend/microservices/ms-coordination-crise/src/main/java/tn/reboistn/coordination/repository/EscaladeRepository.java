package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.model.Escalade;

import java.util.List;

public interface EscaladeRepository extends JpaRepository<Escalade, Long> {

    List<Escalade> findByAlerteId(Long alerteId);

    boolean existsByAlerteId(Long alerteId);
}
