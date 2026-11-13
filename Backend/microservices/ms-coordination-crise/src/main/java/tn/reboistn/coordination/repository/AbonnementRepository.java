package tn.reboistn.coordination.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.reboistn.coordination.model.Abonnement;

import java.util.List;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    List<Abonnement> findByUserId(String userId);

    List<Abonnement> findByGouvernorat(String gouvernorat);

    boolean existsByUserIdAndGouvernorat(String userId, String gouvernorat);
}
