package tn.esprit.forestfirewatch.ressources.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forestfirewatch.ressources.entity.EvenementTraite;

public interface EvenementTraiteRepository extends JpaRepository<EvenementTraite, String> {
}
