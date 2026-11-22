package tn.reboistn.restauration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.restauration.domain.ActionRestauration;

public interface ActionRestaurationRepository extends JpaRepository<ActionRestauration, Long> {
}