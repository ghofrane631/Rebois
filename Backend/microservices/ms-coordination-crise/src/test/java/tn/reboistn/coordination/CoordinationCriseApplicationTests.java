package tn.reboistn.coordination;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CoordinationCriseApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    private long ouvrirIncident(long signalementId) throws Exception {
        String body = """
                {"signalementId": %d, "secteurId": 14, "gouvernorat": "Zaghouan", "niveauRisque": "EXTREME"}
                """.formatted(signalementId);
        String json = mvc.perform(post("/api/incidents").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("OUVERT"))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = mapper.readTree(json);
        return node.get("id").asLong();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void unSeulIncidentParSignalement() throws Exception {
        ouvrirIncident(100);
        mvc.perform(post("/api/incidents").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"signalementId\":100,\"secteurId\":14,\"gouvernorat\":\"Zaghouan\",\"niveauRisque\":\"ELEVE\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void laClotureNePasseQueParLeBilan() throws Exception {
        long id = ouvrirIncident(200);

        // CLOS demandé directement : refusé
        mvc.perform(patch("/api/incidents/" + id + "/statut").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"CLOS\"}"))
                .andExpect(status().isConflict());

        // bilan avant MAITRISE : refusé
        String bilan = "{\"surfaceBruleeHa\":12.5,\"dureeMin\":180,\"cause\":\"Mégot\"}";
        mvc.perform(put("/api/incidents/" + id + "/bilan").contentType(MediaType.APPLICATION_JSON).content(bilan))
                .andExpect(status().isConflict());

        // MAITRISE puis bilan : l'incident est clos
        mvc.perform(patch("/api/incidents/" + id + "/statut").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"statut\":\"MAITRISE\"}"))
                .andExpect(status().isOk());
        mvc.perform(put("/api/incidents/" + id + "/bilan").contentType(MediaType.APPLICATION_JSON).content(bilan))
                .andExpect(status().isOk());
        mvc.perform(get("/api/incidents/" + id))
                .andExpect(jsonPath("$.incident.statut").value("CLOS"))
                .andExpect(jsonPath("$.bilan.surfaceBruleeHa").value(12.5));
    }

    @Test
    void lesAbonnesDeLaZoneSontAlertesParSmsEnRisqueExtreme() throws Exception {
        mvc.perform(post("/api/abonnements").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u-7\",\"gouvernorat\":\"Zaghouan\",\"niveauMin\":\"ELEVE\"}"))
                .andExpect(status().isCreated());
        long id = ouvrirIncident(300);

        mvc.perform(get("/api/incidents/" + id + "/alertes"))
                .andExpect(jsonPath("$[0].userId").value("u-7"))
                .andExpect(jsonPath("$[0].canal").value("SMS"));
    }
}
