package tn.esprit.ffw.risque.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.ffw.risque.entity.*;

public interface SeuilRisqueRepository extends JpaRepository<SeuilRisque, NiveauRisque> { }
