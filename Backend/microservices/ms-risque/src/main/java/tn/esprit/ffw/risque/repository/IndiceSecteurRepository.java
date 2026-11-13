package tn.esprit.ffw.risque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ffw.risque.entity.*;
import java.util.List;

public interface IndiceSecteurRepository extends JpaRepository<IndiceSecteur, Long> {
    List<IndiceSecteur> findByNiveau(NiveauRisque niveau);
}
