package tn.reboistn.restauration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.restauration.domain.EvenementOutbox;

import java.util.List;

public interface EvenementOutboxRepository extends JpaRepository<EvenementOutbox, String> {
    List<EvenementOutbox> findTop100ByPublieLeIsNullOrderByCreeLeAsc();
}