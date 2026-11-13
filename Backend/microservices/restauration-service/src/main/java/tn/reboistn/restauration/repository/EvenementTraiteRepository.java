package tn.reboistn.restauration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.restauration.domain.EvenementTraite;

public interface EvenementTraiteRepository extends JpaRepository<EvenementTraite, String> {
}