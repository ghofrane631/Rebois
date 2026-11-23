package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.entity.Bilan;

public interface BilanRepository extends JpaRepository<Bilan, Long> {
}
