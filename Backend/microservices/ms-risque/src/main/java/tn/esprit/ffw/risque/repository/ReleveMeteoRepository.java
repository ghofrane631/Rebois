package tn.esprit.ffw.risque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.ffw.risque.entity.ReleveMeteo;
import java.time.LocalDateTime;
import java.util.List;

public interface ReleveMeteoRepository extends JpaRepository<ReleveMeteo, Long> {
    @Query("""
           select r from ReleveMeteo r
           where (:secteurId is null or r.secteurId = :secteurId)
             and r.dateReleve >= :from and r.dateReleve <= :to
           order by r.dateReleve desc
           """)
    List<ReleveMeteo> rechercher(@Param("secteurId") Long secteurId,
                                 @Param("from") LocalDateTime from,
                                 @Param("to") LocalDateTime to);
}
