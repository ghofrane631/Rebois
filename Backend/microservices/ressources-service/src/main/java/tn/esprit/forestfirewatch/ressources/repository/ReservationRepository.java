package tn.esprit.forestfirewatch.ressources.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forestfirewatch.ressources.entity.Reservation;
import tn.esprit.forestfirewatch.ressources.entity.StatutReservation;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByIncidentId(Long incidentId);

    List<Reservation> findByIncidentIdAndStatut(Long incidentId, StatutReservation statut);
}
