package com.esprit.ffw.signalement.controller;

import com.esprit.ffw.signalement.config.SecurityConfig;
import com.esprit.ffw.signalement.dto.SignalementResponse;
import com.esprit.ffw.signalement.model.Signalement;
import com.esprit.ffw.signalement.model.Statut;
import com.esprit.ffw.signalement.service.SignalementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SignalementController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SignalementControllerSecurityTest {

    private static final String CREATION = """
            {"type":"FUMEE","latitude":36.42,"longitude":10.08,"intensite":3,"description":"fumée"}
            """;

    @Autowired MockMvc mvc;
    @MockBean SignalementService service;
    @MockBean JwtDecoder jwtDecoder;

    private Signalement signalement() {
        Signalement s = new Signalement();
        s.setId("1");
        s.setAuteurId("u-12");
        s.setStatut(Statut.CONFIRME);
        s.setPosition(new GeoJsonPoint(10.08, 36.42));
        return s;
    }

    @Test
    void creationSansJetonEstRefusee() throws Exception {
        mvc.perform(post("/api/signalements").contentType(MediaType.APPLICATION_JSON).content(CREATION))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "BENEVOLE")
    void creationAvecBenevoleEstAcceptee() throws Exception {
        when(service.creer(any(), anyString()))
                .thenReturn(new SignalementResponse(signalement(), List.of()));

        mvc.perform(post("/api/signalements").contentType(MediaType.APPLICATION_JSON).content(CREATION))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "GARDE")
    void confirmationAvecGardeEstAcceptee() throws Exception {
        when(service.confirmer(eq("1"), anyString(), any())).thenReturn(signalement());

        mvc.perform(patch("/api/signalements/1/confirmer")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "BENEVOLE")
    void confirmationAvecBenevoleEstRefusee() throws Exception {
        mvc.perform(patch("/api/signalements/1/confirmer")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void feuxActifsSontPublics() throws Exception {
        when(service.actifs()).thenReturn(List.of());

        mvc.perform(get("/api/signalements/actifs")).andExpect(status().isOk());
    }

    @Test
    void listeSansJetonEstRefusee() throws Exception {
        mvc.perform(get("/api/signalements")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "BENEVOLE")
    void statutInconnuRetourne400() throws Exception {
        mvc.perform(get("/api/signalements").param("statut", "INCONNU"))
                .andExpect(status().isBadRequest());
    }
}
