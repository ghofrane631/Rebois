package tn.esprit.forestfirewatch.ressources;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.ReservationRequest;
import tn.esprit.forestfirewatch.ressources.dto.Dtos.ReservationResultat;
import tn.esprit.forestfirewatch.ressources.entity.*;
import tn.esprit.forestfirewatch.ressources.event.Evenements.InterventionTerminee;
import tn.esprit.forestfirewatch.ressources.event.Evenements.NiveauRisqueChange;
import tn.esprit.forestfirewatch.ressources.exception.ApiException;
import tn.esprit.forestfirewatch.ressources.repository.*;
import tn.esprit.forestfirewatch.ressources.service.EvenementService;
import tn.esprit.forestfirewatch.ressources.service.ReservationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tn.esprit.forestfirewatch.ressources.entity.TypeRessource.CAMION_CITERNE;

/** Règles métier : proximité, réserve minimale, tout ou rien, libération idempotente. */
@SpringBootTest
class ReservationServiceTest {

    // Feu près de Zaghouan
    private static final double LAT_FEU = 36.42;
    private static final double LNG_FEU = 10.08;

    @Autowired ReservationService reservationService;
    @Autowired EvenementService evenementService;
    @Autowired RessourceRepository ressourceRepository;
    @Autowired ReservationRepository reservationRepository;
    @Autowired PrePositionnementRepository prePositionnementRepository;
    @Autowired EvenementTraiteRepository evenementTraiteRepository;
    @Autowired OutboxEventRepository outboxRepository;

    @BeforeEach
    void donnees() {
        reservationRepository.deleteAll();
        prePositionnementRepository.deleteAll();
        evenementTraiteRepository.deleteAll();
        outboxRepository.deleteAll();
        ressourceRepository.deleteAll();
        // Caserne 1 (Zaghouan, proche du feu) : 3 camions ; caserne 2 (Jendouba, loin) : 2 camions
        ressourceRepository.save(new Ressource(CAMION_CITERNE, 1L, "Zaghouan", 36.40, 10.14));
        ressourceRepository.save(new Ressource(CAMION_CITERNE, 1L, "Zaghouan", 36.40, 10.14));
        ressourceRepository.save(new Ressource(CAMION_CITERNE, 1L, "Zaghouan", 36.40, 10.14));
        ressourceRepository.save(new Ressource(CAMION_CITERNE, 2L, "Jendouba", 36.50, 8.78));
        ressourceRepository.save(new Ressource(CAMION_CITERNE, 2L, "Jendouba", 36.50, 8.78));
    }

    private ReservationRequest demande(long incidentId, int nombre) {
        return new ReservationRequest(incidentId, CAMION_CITERNE, nombre, LAT_FEU, LNG_FEU);
    }

    @Test
    void reserveLesCamionsLesPlusProches() {
        ReservationResultat r = reservationService.reserver(demande(5, 2));

        // Les 2 camions viennent de Zaghouan (le plus proche), pas de Jendouba
        assertThat(r.ressources()).hasSize(2);
        r.ressources().forEach(id -> {
            Ressource camion = ressourceRepository.findById(id).orElseThrow();
            assertThat(camion.getUniteId()).isEqualTo(1L);
            assertThat(camion.getStatut()).isEqualTo(StatutRessource.RESERVEE);
        });
    }

    @Test
    void laCaserneGardeToujoursUnCamion() {
        // 3 demandés : Zaghouan n'en donne que 2 (garde 1), Jendouba en donne 1 (garde 1)
        ReservationResultat r = reservationService.reserver(demande(5, 3));

        assertThat(r.ressources()).hasSize(3);
        assertThat(ressourceRepository.countByUniteIdAndTypeAndStatut(1L, CAMION_CITERNE, StatutRessource.DISPONIBLE))
                .isEqualTo(1);
        assertThat(ressourceRepository.countByUniteIdAndTypeAndStatut(2L, CAMION_CITERNE, StatutRessource.DISPONIBLE))
                .isEqualTo(1);
    }

    @Test
    void toutOuRienEtMoyensInsuffisantsPublie() {
        // 4 demandés, 3 réservables au maximum : rien n'est réservé, 409, événement dans l'outbox
        assertThatThrownBy(() -> reservationService.reserver(demande(6, 4)))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("code", "AUCUN_MOYEN_DISPONIBLE");

        assertThat(reservationRepository.count()).isZero();
        assertThat(outboxRepository.findByType("MoyensInsuffisants")).hasSize(1);
    }

    @Test
    void interventionTermineeLibereLesMoyensUneSeuleFois() {
        reservationService.reserver(demande(7, 2));

        InterventionTerminee evt = new InterventionTerminee("evt-42", 7L, 14L, 12.5);
        evenementService.surInterventionTerminee(evt);
        evenementService.surInterventionTerminee(evt); // doublon : ignoré

        assertThat(reservationRepository.findByIncidentIdAndStatut(7L, StatutReservation.LIBEREE)).hasSize(2);
        assertThat(ressourceRepository.findByStatut(StatutRessource.RESERVEE)).isEmpty();
        assertThat(evenementTraiteRepository.count()).isEqualTo(1);
    }

    @Test
    void risqueExtremePrePositionneUnCamion() {
        evenementService.surNiveauRisqueChange(new NiveauRisqueChange("evt-99", 14L, "Zaghouan",
                LAT_FEU, LNG_FEU, "TRES_ELEVE", "EXTREME"));

        assertThat(prePositionnementRepository.findBySecteurId(14L)).hasSize(1);
    }
}
