package com.esprit.ffw.signalement.service;

import com.esprit.ffw.signalement.client.ForetClient;
import com.esprit.ffw.signalement.client.SecteurDto;
import com.esprit.ffw.signalement.dto.SignalementRequest;
import com.esprit.ffw.signalement.dto.SignalementResponse;
import com.esprit.ffw.signalement.event.EventPublisher;
import com.esprit.ffw.signalement.exception.SignalementIntrouvableException;
import com.esprit.ffw.signalement.exception.TransitionInvalideException;
import com.esprit.ffw.signalement.model.*;
import com.esprit.ffw.signalement.repository.SignalementRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalementService {

    static final double RAYON_DOUBLON_M = 500;
    static final int FENETRE_DOUBLON_MIN = 30;
    private static final Set<Statut> A_TRAITER = Set.of(Statut.SIGNALE, Statut.SECTEUR_A_VERIFIER);
    private static final Set<Statut> ACTIFS = Set.of(Statut.SIGNALE, Statut.SECTEUR_A_VERIFIER, Statut.CONFIRME);

    private final SignalementRepository repo;
    private final ForetClient foret;
    private final EventPublisher publisher;

    // ---------- création
    public SignalementResponse creer(SignalementRequest r, String auteurId) {
        List<String> doublons = proches(r.latitude(), r.longitude(), RAYON_DOUBLON_M, FENETRE_DOUBLON_MIN)
                .stream().map(Signalement::getId).toList();

        Signalement s = new Signalement();
        s.setAuteurId(auteurId);
        s.setType(r.type());
        s.setPosition(new GeoJsonPoint(r.longitude(), r.latitude()));   // [lng, lat]
        s.setIntensite(r.intensite());
        s.setDescription(r.description());
        s.setCreeLe(LocalDateTime.now());
        if (r.photoUrl() != null && !r.photoUrl().isBlank()) {
            s.getPhotos().add(new Photo(r.photoUrl(), LocalDateTime.now()));
        }
        // Repli : si Forêt est indisponible on ne perd jamais un feu
        s.setStatut(rattacherSecteur(s) ? Statut.SIGNALE : Statut.SECTEUR_A_VERIFIER);
        return new SignalementResponse(repo.save(s), doublons);
    }

    // ---------- confirmation / fausse alerte
    public Signalement confirmer(String id, String gardeId, String motif) {
        Signalement s = trouver(id);
        transition(s, Statut.CONFIRME, gardeId, motif);
        if (s.getSecteurId() == null) rattacherSecteur(s);   // nouvelle tentative
        Signalement saved = repo.save(s);
        publisher.publierFeuConfirme(saved);
        return saved;
    }

    public Signalement fausseAlerte(String id, String gardeId, String motif) {
        Signalement s = trouver(id);
        transition(s, Statut.FAUSSE_ALERTE, gardeId, motif);
        return repo.save(s);
    }

    // ---------- photos
    public Signalement ajouterPhoto(String id, String url, String userId) {
        Signalement s = trouver(id);
        if (!s.getAuteurId().equals(userId)) {
            throw new AccessDeniedException("Seul l'auteur peut ajouter une photo");
        }
        s.getPhotos().add(new Photo(url, LocalDateTime.now()));
        return repo.save(s);
    }

    // ---------- lecture
    public Signalement trouver(String id) {
        return repo.findById(id).orElseThrow(() -> new SignalementIntrouvableException(id));
    }

    public List<Signalement> rechercher(Long secteurId, Statut statut) {
        if (secteurId != null && statut != null) return repo.findBySecteurIdAndStatut(secteurId, statut);
        if (secteurId != null) return repo.findBySecteurId(secteurId);
        if (statut != null) return repo.findByStatut(statut);
        return repo.findAll();
    }

    public List<Signalement> actifs() { return repo.findByStatut(Statut.CONFIRME); }

    /** Signalements encore actifs dans un rayon (mètres) et une fenêtre de temps (minutes). */
    public List<Signalement> proches(double lat, double lng, double rayonM, int minutes) {
        return repo.findByPositionNearAndCreeLeAfter(new Point(lng, lat),
                        new Distance(rayonM / 1000.0, Metrics.KILOMETERS),
                        LocalDateTime.now().minusMinutes(minutes))
                .stream().filter(s -> ACTIFS.contains(s.getStatut())).toList();
    }

    // ---------- fin d'intervention (événement InterventionTerminee)
    public void cloreApresIntervention(String signalementId, Long secteurId) {
        List<Signalement> cibles = signalementId != null
                ? repo.findById(signalementId).map(List::of).orElse(List.of())
                : (secteurId != null ? repo.findBySecteurIdAndStatut(secteurId, Statut.CONFIRME) : List.of());
        for (Signalement s : cibles) {
            if (s.getStatut() == Statut.CONFIRME) {            // idempotent : déjà CLOS = ignoré
                s.getHistorique().add(new HistoriqueStatut(s.getStatut(), Statut.CLOS, "system",
                        "Intervention terminée", LocalDateTime.now()));
                s.setStatut(Statut.CLOS);
                repo.save(s);
            }
        }
    }

    // ---------- utilitaires
    private boolean rattacherSecteur(Signalement s) {
        try {
            SecteurDto sec = foret.localiser(s.getPosition().getY(), s.getPosition().getX());
            s.setSecteurId(sec.id());
            return true;
        } catch (Exception e) {
            log.warn("Forêt indisponible ou position hors forêt : {}", e.getMessage());
            return false;
        }
    }

    private void transition(Signalement s, Statut nouveau, String gardeId, String motif) {
        if (!A_TRAITER.contains(s.getStatut())) {
            throw new TransitionInvalideException(
                    "Transition interdite : " + s.getStatut() + " → " + nouveau);
        }
        s.getHistorique().add(new HistoriqueStatut(s.getStatut(), nouveau, gardeId, motif, LocalDateTime.now()));
        s.setStatut(nouveau);
    }
}
