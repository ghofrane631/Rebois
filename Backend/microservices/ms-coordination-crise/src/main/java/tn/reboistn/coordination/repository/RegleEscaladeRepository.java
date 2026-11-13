package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.model.RegleEscalade;
import tn.reboistn.coordination.model.TypeAlerte;

public interface RegleEscaladeRepository extends JpaRepository<RegleEscalade, Long> {

    boolean existsByTypeAlerte(TypeAlerte typeAlerte);
}
