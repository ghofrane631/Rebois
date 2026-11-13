package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.model.RegleEscalade;
import tn.reboistn.coordination.model.TypeAlerte;

import java.util.Optional;

public interface RegleEscaladeRepository extends JpaRepository<RegleEscalade, Long> {

    boolean existsByTypeAlerte(TypeAlerte typeAlerte);

    Optional<RegleEscalade> findByTypeAlerte(TypeAlerte typeAlerte);
}
