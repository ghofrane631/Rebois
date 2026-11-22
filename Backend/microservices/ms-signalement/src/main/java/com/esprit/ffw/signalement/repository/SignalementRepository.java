package com.esprit.ffw.signalement.repository;

import com.esprit.ffw.signalement.model.Signalement;
import com.esprit.ffw.signalement.model.Statut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.geo.Distance;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface SignalementRepository extends MongoRepository<Signalement, String> {
    List<Signalement> findBySecteurId(Long secteurId);
    List<Signalement> findByStatut(Statut statut);
    List<Signalement> findBySecteurIdAndStatut(Long secteurId, Statut statut);

    Page<Signalement> findBySecteurId(Long secteurId, Pageable pageable);
    Page<Signalement> findByStatut(Statut statut, Pageable pageable);
    Page<Signalement> findBySecteurIdAndStatut(Long secteurId, Statut statut, Pageable pageable);

    /** $near sur l'index 2dsphere : signalements dans un rayon (mètres) et plus récents que "since". */
    List<Signalement> findByPositionNearAndCreeLeAfter(GeoJsonPoint position, Distance distance, Instant since);
}
