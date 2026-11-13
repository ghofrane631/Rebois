package tn.esprit.ffw.risque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ffw.risque.entity.FeuRecent;
import java.time.LocalDateTime;

public interface FeuRecentRepository extends JpaRepository<FeuRecent, Long> {
    int countBySecteurIdAndEteintFalseAndDateConfirmationAfter(Long secteurId, LocalDateTime depuis);
}
