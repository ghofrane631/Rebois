package tn.esprit.ffw.risque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ffw.risque.entity.HistoriqueNiveau;
import java.util.List;

public interface HistoriqueNiveauRepository extends JpaRepository<HistoriqueNiveau, Long> {
    List<HistoriqueNiveau> findBySecteurIdOrderByDateDesc(Long secteurId);
}
