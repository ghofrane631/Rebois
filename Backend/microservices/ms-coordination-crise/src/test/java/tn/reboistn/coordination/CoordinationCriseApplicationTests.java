package tn.reboistn.coordination;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tn.reboistn.coordination.event.FeuConfirme;
import tn.reboistn.coordination.event.InterventionTerminee;
import tn.reboistn.coordination.model.AlerteEmise;
import tn.reboistn.coordination.repository.AlerteEmiseRepository;
import tn.reboistn.coordination.repository.IncidentRepository;
import tn.reboistn.coordination.service.EscaladeService;
import tn.reboistn.coordination.service.EvenementService;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Ressources, Signalement et User ne tournent pas pendant les tests :
 * chaque appel OpenFeign échoue, ce qui vérifie aussi les comportements de repli.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CoordinationCriseApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private EvenementService evenementService;

    @Autowired
    private EscaladeService escaladeService;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AlerteEmiseRepository alerteRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private long ouvrirIncident(long signalementId, String gouvernorat) throws Exception {
        String body = """
                {"signalementId": %d, "secteurId": 14, "gouvernorat": "%s", "niveauRisque": "EXTREME",
                 "latitude": 36.42, "longitude": 10.08}
                """.formatted(signalementId, gouvernorat);
        String json = mvc.perform(post("/api/incidents").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(json);
        return node.get("id").asLong();
    }

    private void patchStatut(long id, String statut, int attendu) throws Exception {
        mvc.perform(patch("/api/incidents/" + id + "/statut").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"" + statut + "\"}"))
                .andExpect(status().is(attendu));
    }

    @Test
    void contextLoads() {
    }

    @Test
    void unSeulIncidentParSignalement() throws Exception {
        ouvrirIncident(100, "Zaghouan");
        mvc.perform(post("/api/incidents").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signalementId\":100,\"secteurId\":14,\"gouvernorat\":\"Zaghouan\",\"niveauRisque\":\"ELEVE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void ressourcesIndisponiblePasseEnMoyensAAffecter() throws Exception {
        long id = ouvrirIncident(150, "Beja");
        mvc.perform(get("/api/incidents/" + id))
                .andExpect(jsonPath("$.incident.statut").value("MOYENS_A_AFFECTER"));
        mvc.perform(post("/api/incidents/" + id + "/moyens").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"CAMION_CITERNE\",\"nombre\":2}"))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void transitionInterdite() throws Exception {
        long id = ouvrirIncident(160, "Beja");
        patchStatut(id, "MAITRISE", 409);   // MOYENS_A_AFFECTER -> MAITRISE interdit
        patchStatut(id, "EN_COURS", 200);
    }

    @Test
    void laClotureNePasseQueParLeBilanEtPublieInterventionTerminee() throws Exception {
        long id = ouvrirIncident(200, "Jendouba");
        patchStatut(id, "EN_COURS", 200);
        patchStatut(id, "CLOS", 409);

        String bilan = "{\"surfaceBruleeHa\":12.5,\"dureeMin\":180,\"cause\":\"Mégot\"}";
        mvc.perform(put("/api/incidents/" + id + "/bilan").contentType(MediaType.APPLICATION_JSON).content(bilan))
                .andExpect(status().isConflict());

        patchStatut(id, "MAITRISE", 200);
        mvc.perform(put("/api/incidents/" + id + "/bilan").contentType(MediaType.APPLICATION_JSON).content(bilan))
                .andExpect(status().isOk());
        mvc.perform(get("/api/incidents/" + id))
                .andExpect(jsonPath("$.incident.statut").value("CLOS"))
                .andExpect(jsonPath("$.bilan.surfaceBruleeHa").value(12.5));
        verify(rabbitTemplate).convertAndSend(eq("ffw.events"), eq("InterventionTerminee"), any(InterventionTerminee.class));
    }

    @Test
    void lesAbonnesDeLaZoneSontAlertesParSmsEnRisqueExtreme() throws Exception {
        mvc.perform(post("/api/abonnements").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u-7\",\"gouvernorat\":\"Bizerte\",\"niveauMin\":\"ELEVE\"}"))
                .andExpect(status().isCreated());
        long id = ouvrirIncident(300, "Bizerte");

        mvc.perform(get("/api/incidents/" + id + "/alertes"))
                .andExpect(jsonPath("$[0].userId").value("u-7"))
                .andExpect(jsonPath("$[0].canal").value("SMS"));
    }

    @Test
    void feuConfirmeOuvreUnIncidentUneSeuleFois() {
        FeuConfirme evenement = new FeuConfirme("evt-1", 400L, 9L, "Nabeul", 36.5, 10.7, 4, "TRES_ELEVE");
        evenementService.surFeuConfirme(evenement);
        evenementService.surFeuConfirme(evenement);   // message reçu deux fois

        assertThat(incidentRepository.findAll().stream().filter(i -> i.getSignalementId() == 400L)).hasSize(1);
    }

    @Test
    void uneAlerteNonLueEstEscaladeeUneSeuleFois() throws Exception {
        mvc.perform(post("/api/abonnements").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u-9\",\"gouvernorat\":\"Siliana\",\"niveauMin\":\"FAIBLE\"}"))
                .andExpect(status().isCreated());
        long id = ouvrirIncident(500, "Siliana");
        AlerteEmise alerte = alerteRepository.findByIncident_Id(id).get(0);
        alerte.setEmiseLe(LocalDateTime.now().minusMinutes(15));   // plus vieille que le délai de 10 min
        alerteRepository.save(alerte);

        escaladeService.escaladerAlertesNonLues();
        escaladeService.escaladerAlertesNonLues();

        mvc.perform(get("/api/alertes/" + alerte.getId() + "/escalades"))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].regle.cible").value("CASERNE_REGIONALE"));
    }
}
