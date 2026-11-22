package tn.reboistn.restauration.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import tn.reboistn.restauration.client.ForetClient;
import tn.reboistn.restauration.domain.ZoneSinistree;
import tn.reboistn.restauration.event.ZoneEventPublisher;
import tn.reboistn.restauration.repository.ZoneSinistreeRepository;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class RestaurationApiIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ZoneSinistreeRepository zoneRepository;

    @MockBean
    private ForetClient foretClient;

    @MockBean
    private ZoneEventPublisher eventPublisher;

    private Long zoneId;

    @BeforeEach
    void setUp() {
        zoneRepository.deleteAll();
        ZoneSinistree zone = zoneRepository.save(
                new ZoneSinistree(100L, 10L, new BigDecimal("8.50"), "Jendouba"));
        zoneId = zone.getId();
    }

    @Test
    void planWithoutEvaluationReturnsConflict() {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/restauration/zones/{id}/plan",
                java.util.Map.of("budgetEstime", 120000), String.class, zoneId);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody().contains("EVALUATION_REQUISE"));
    }

        @Test
        void listAndDetailRoutesReturnFilteredZone() {
                ResponseEntity<String> list = restTemplate.getForEntity(
                                "/api/restauration/zones?statut=A_EVALUER&gouvernorat=Jendouba", String.class);
                ResponseEntity<String> detail = restTemplate.getForEntity(
                                "/api/restauration/zones/{id}", String.class, zoneId);

                assertEquals(HttpStatus.OK, list.getStatusCode());
                assertTrue(list.getBody().contains("Jendouba"));
                assertEquals(HttpStatus.OK, detail.getStatusCode());
                assertTrue(detail.getBody().contains("100"));
        }

    @Test
    void evaluationPlanAndSeventyFivePercentFollowupRestoreZone() {
        ResponseEntity<String> evaluation = restTemplate.postForEntity(
                "/api/restauration/zones/{id}/evaluation",
                java.util.Map.of("arbresPerdus", 120, "etatSol", "DEGRADE",
                        "risqueErosion", true, "gravite", "FORTE"),
                String.class, zoneId);
        assertEquals(HttpStatus.CREATED, evaluation.getStatusCode());
        assertTrue(evaluation.getBody().contains("EVALUEE"));

        ResponseEntity<String> plan = restTemplate.postForEntity(
                "/api/restauration/zones/{id}/plan",
                java.util.Map.of("budgetEstime", 120000,
                        "actions", java.util.List.of(java.util.Map.of(
                                "type", "REBOISEMENT", "description", "Planter des arbres"))),
                String.class, zoneId);
        assertEquals(HttpStatus.CREATED, plan.getStatusCode());
        assertTrue(plan.getBody().contains("EN_RESTAURATION"));

        ResponseEntity<String> suivi = restTemplate.postForEntity(
                "/api/restauration/zones/{id}/suivis",
                java.util.Map.of("date", "2026-11-13", "couverturePct", 75, "gardeId", "garde-1"),
                String.class, zoneId);
        assertEquals(HttpStatus.CREATED, suivi.getStatusCode());
        assertTrue(suivi.getBody().contains("RESTAUREE"));
        verify(eventPublisher).publish(any());
    }
}