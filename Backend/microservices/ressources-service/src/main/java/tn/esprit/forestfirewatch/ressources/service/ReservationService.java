package tn.esprit.forestfirewatch.ressources.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.ReservationRequest;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.ReservationResponse;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.ReservationResultat;
import tn.esprit.forestfirewatch.ressources.entity.*;
import tn.esprit.forestfirewatch.ressources.event.Evenements.MoyensInsuffisants;
import tn.esprit.forestfirewatch.ressources.exception.ApiException;
import tn.esprit.forestfirewatch.ressources.repository.ReservationRepository;
import tn.esprit.forestfirewatch.ressources.repository.RessourceRepository;

import java.util.*;

/**
 * Cœur métier du service : réserver les moyens les plus proches d'un feu.
 *
 * Règles :
 *  1. Les moyens sont choisis du plus proche au plus éloigné du feu.
 *  2. Réserve minimale : une caserne garde toujours au moins N camions-citernes disponibles.
 *  3. Tout ou rien : si on ne trouve pas assez de moyens, rien n'est réservé,
 *     MoyensInsuffisants est publié et l'appelant reçoit 409.
 *  4. Verrouillage optimiste : si un autre incident prend le même moyen au même moment,
 *     on recommence avec les moyens libres restants (3 essais maximum).
 */
@Service
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);
    private static final int ESSAIS_MAX = 3;

    private final RessourceRepository ressourceRepository;
    private final ReservationRepository reservationRepository;
    private final OutboxService outboxService;
    private final TransactionTemplate transaction;
    private final int reserveMinimale;

    public ReservationService(RessourceRepository ressourceRepository,
                              ReservationRepository reservationRepository,
                              OutboxService outboxService,
                              PlatformTransactionManager transactionManager,
                              @Value("${ressources.reserve-minimale-camions:1}") int reserveMinimale) {
        this.ressourceRepository = ressourceRepository;
        this.reservationRepository = reservationRepository;
        this.outboxService = outboxService;
        this.transaction = new TransactionTemplate(transactionManager);
        this.reserveMinimale = reserveMinimale;
    }

    /** Résultat interne d'une tentative : soit des réservations, soit le nombre de moyens trouvés. */
    private record Tentative(ReservationResultat resultat, int trouves) {
    }

    public ReservationResultat reserver(ReservationRequest req) {
        for (int essai = 1; essai <= ESSAIS_MAX; essai++) {
            try {
                Tentative t = transaction.execute(status -> tenterReservation(req));
                if (t.resultat() != null) {
                    return t.resultat();
                }
                signalerMoyensInsuffisants(req, t.trouves());
                throw ApiException.conflict("AUCUN_MOYEN_DISPONIBLE",
                        "Demandés : " + req.nombre() + " " + req.type() + ", disponibles : " + t.trouves());
            } catch (ObjectOptimisticLockingFailureException e) {
                log.info("Conflit de version sur un moyen (essai {}/{}), nouvel essai", essai, ESSAIS_MAX);
            }
        }
        throw ApiException.conflict("RESSOURCE_DEJA_RESERVEE",
                "Moyens pris par un autre incident pendant la réservation, réessayer");
    }

    private Tentative tenterReservation(ReservationRequest req) {
        List<Ressource> libres = new ArrayList<>(
                ressourceRepository.findByTypeAndStatut(req.type(), StatutRessource.DISPONIBLE));

        // Règle 1 : du plus proche au plus éloigné du feu.
        libres.sort(Comparator.comparingDouble(
                r -> Distance.km(req.lat(), req.lng(), r.getLatitude(), r.getLongitude())));

        // Règle 2 : nombre de moyens libres par caserne, pour respecter la réserve minimale.
        Map<Long, Integer> libresParCaserne = new HashMap<>();
        libres.forEach(r -> libresParCaserne.merge(r.getUniteId(), 1, Integer::sum));

        List<Ressource> choisis = new ArrayList<>();
        for (Ressource r : libres) {
            if (choisis.size() == req.nombre()) {
                break;
            }
            int restants = libresParCaserne.get(r.getUniteId());
            if (req.type() == TypeRessource.CAMION_CITERNE && restants - 1 < reserveMinimale) {
                continue; // la caserne doit garder un camion pour un autre feu
            }
            choisis.add(r);
            libresParCaserne.put(r.getUniteId(), restants - 1);
        }

        // Règle 3 : tout ou rien.
        if (choisis.size() < req.nombre()) {
            return new Tentative(null, choisis.size());
        }

        List<ReservationResponse> reservations = new ArrayList<>();
        for (Ressource r : choisis) {
            r.setStatut(StatutRessource.RESERVEE);
            reservations.add(ReservationResponse.of(reservationRepository.save(new Reservation(r, req.incidentId()))));
        }
        // Règle 4 : le flush déclenche le contrôle de version (UPDATE ... WHERE version = ?).
        ressourceRepository.flush();

        List<Long> ids = choisis.stream().map(Ressource::getId).toList();
        return new Tentative(new ReservationResultat(req.incidentId(), ids, reservations), choisis.size());
    }

    private void signalerMoyensInsuffisants(ReservationRequest req, int trouves) {
        transaction.executeWithoutResult(status -> outboxService.enregistrer("MoyensInsuffisants",
                new MoyensInsuffisants(req.incidentId(), null, null, req.type().name(),
                        req.nombre(), trouves, "RESERVATION_IMPOSSIBLE")));
    }

    @Transactional(readOnly = true)
    public List<Reservation> parIncident(Long incidentId) {
        return reservationRepository.findByIncidentId(incidentId);
    }

    /** DELETE /api/ressources/reservations/{id} : libération manuelle. Idempotent. */
    @Transactional
    public void liberer(Long reservationId) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> ApiException.notFound("RESERVATION_INCONNUE",
                        "Aucune réservation avec l'id " + reservationId));
        if (r.getStatut() == StatutReservation.ACTIVE) {
            r.liberer();
        }
    }

    /** Appelé à la réception de InterventionTerminee : libère tous les moyens de l'incident. */
    @Transactional
    public int libererIncident(Long incidentId) {
        List<Reservation> actives = reservationRepository.findByIncidentIdAndStatut(incidentId, StatutReservation.ACTIVE);
        actives.forEach(Reservation::liberer);
        return actives.size();
    }
}
