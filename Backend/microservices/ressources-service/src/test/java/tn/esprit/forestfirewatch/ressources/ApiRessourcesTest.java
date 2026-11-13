package tn.esprit.forestfirewatch.ressources;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Codes de réponse du contrat d'API : 201, 400, 404. */
@SpringBootTest
@AutoConfigureMockMvc
class ApiRessourcesTest {

    @Autowired MockMvc mvc;

    @Test
    void recenserUnMoyen201() throws Exception {
        mvc.perform(post("/api/ressources").contentType(MediaType.APPLICATION_JSON).content("""
                        {"type":"HELICOPTERE","uniteId":2,"gouvernorat":"Jendouba","latitude":36.50,"longitude":8.78}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("DISPONIBLE"));
    }

    @Test
    void requeteInvalide400() throws Exception {
        mvc.perform(post("/api/ressources/reservations").contentType(MediaType.APPLICATION_JSON).content("""
                        {"incidentId":5,"type":"CAMION_CITERNE","nombre":0,"lat":36.42,"lng":10.08}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("REQUETE_INVALIDE"));
    }

    @Test
    void moyenInconnu404() throws Exception {
        mvc.perform(get("/api/ressources/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("RESSOURCE_INCONNUE"));
    }
}
