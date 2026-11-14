package com.esprit.ffw.signalement.repository;

import com.esprit.ffw.signalement.model.Signalement;
import com.esprit.ffw.signalement.model.Statut;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SignalementRepository extends MongoRepository<Signalement, String> {
    List<Signalement> findBySecteurId(Long secteurId);
    List<Signalement> findByStatut(Statut statut);
    List<Signalement> findBySecteurIdAndStatut(Long secteurId, Statut statut);

    /** $near sur l'index 2dsphere : signalements dans un rayon et plus récents que "since". */
    List<Signalement> findByPositionNearAndCreeLeAfter(Point point, Distance distance, LocalDateTime since);
}
