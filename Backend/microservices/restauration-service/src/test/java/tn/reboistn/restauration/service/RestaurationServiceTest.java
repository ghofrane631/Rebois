package tn.reboistn.restauration.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.reboistn.restauration.api.dto.PlanRequest;
import tn.reboistn.restauration.api.dto.SuiviRequest;
import tn.reboistn.restauration.api.dto.ZoneResponse;
import tn.reboistn.restauration.client.ForetClient;
import tn.reboistn.restauration.domain.EtatSol;
import tn.reboistn.restauration.domain.EvaluationDegats;
import tn.reboistn.restauration.domain.PlanRestauration;
import tn.reboistn.restauration.domain.StatutZone;
import tn.reboistn.restauration.domain.ZoneSinistree;
import tn.reboistn.restauration.event.InterventionTermineeEvent;
import tn.reboistn.restauration.event.ZoneEventPublisher;
import tn.reboistn.restauration.event.ZoneRestaureeEvent;
import tn.reboistn.restauration.exception.ConflictException;
import tn.reboistn.restauration.repository.ActionRestaurationRepository;
import tn.reboistn.restauration.repository.EvenementTraiteRepository;
import tn.reboistn.restauration.repository.ZoneSinistreeRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestaurationServiceTest {

    @Mock
    private ZoneSinistreeRepository zoneRepository;
    @Mock
    private ActionRestaurationRepository actionRepository;
    @Mock
    private EvenementTraiteRepository evenementRepository;
    @Mock
    private ForetClient foretClient;
    @Mock
    private ZoneEventPublisher eventPublisher;
    @InjectMocks
    private RestaurationService restaurationService;

    private ZoneSinistree zone;

    @BeforeEach
    void setUp() {
        zone = new ZoneSinistree(42L, 7L, new BigDecimal("12.50"), "Jendouba");
        zone.setEvaluation(new EvaluationDegats(120, EtatSol.DEGRADE, true, null));
        zone.setPlan(new PlanRestauration("Pin d'Alep", new BigDecimal("120000.00")));
    }

    @Test
    void planRequiresDamageEvaluation() {
        ZoneSinistree zoneSansEvaluation = new ZoneSinistree(43L, 8L, BigDecimal.ONE, "Jendouba");
        when(zoneRepository.findById(2L)).thenReturn(Optional.of(zoneSansEvaluation));

        assertThrows(ConflictException.class,
                () -> restaurationService.createPlan(2L, new PlanRequest(BigDecimal.ONE, null)));
    }

    @Test
    void coverageBelowSeventyDoesNotRestoreZone() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(zoneRepository.save(any(ZoneSinistree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ZoneResponse response = restaurationService.addSuivi(1L,
                new SuiviRequest(LocalDate.now(), 69, "garde-1"));

        assertEquals(StatutZone.EN_RESTAURATION, response.statut());
        verify(eventPublisher, never()).publish(any(ZoneRestaureeEvent.class));
    }

    @Test
    void coverageAtSeventyRestoresZoneAndPublishesEvent() {
        when(zoneRepository.findById(1L)).thenReturn(Optional.of(zone));
        when(zoneRepository.save(any(ZoneSinistree.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ZoneResponse response = restaurationService.addSuivi(1L,
                new SuiviRequest(LocalDate.now(), 70, "garde-1"));

        assertEquals(StatutZone.RESTAUREE, response.statut());
        verify(eventPublisher).publish(any(ZoneRestaureeEvent.class));
    }

    @Test
    void duplicateInterventionEventIsIgnored() {
        when(evenementRepository.existsById("evt-1")).thenReturn(true);

        restaurationService.createZoneFromIntervention(new InterventionTermineeEvent(
                "evt-1", 42L, 7L, new BigDecimal("12.50"), "Jendouba"));

        verify(zoneRepository, never()).save(any(ZoneSinistree.class));
    }
}