package tn.reboistn.restauration.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.restauration.domain.StatutZone;
import tn.reboistn.restauration.domain.ZoneSinistree;

import java.util.List;
import java.util.Optional;

public interface ZoneSinistreeRepository extends JpaRepository<ZoneSinistree, Long> {
    Optional<ZoneSinistree> findByIncidentId(Long incidentId);
    List<ZoneSinistree> findAllByStatut(StatutZone statut);
    List<ZoneSinistree> findAllByGouvernoratIgnoreCase(String gouvernorat);
    List<ZoneSinistree> findAllByStatutAndGouvernoratIgnoreCase(StatutZone statut, String gouvernorat);
}