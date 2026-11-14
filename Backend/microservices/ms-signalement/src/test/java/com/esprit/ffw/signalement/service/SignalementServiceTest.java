package com.esprit.ffw.signalement.service;

import com.esprit.ffw.signalement.client.ForetClient;
import com.esprit.ffw.signalement.client.SecteurDto;
import com.esprit.ffw.signalement.dto.SignalementRequest;
import com.esprit.ffw.signalement.event.EventPublisher;
import com.esprit.ffw.signalement.exception.TransitionInvalideException;
import com.esprit.ffw.signalement.model.*;
import com.esprit.ffw.signalement.repository.SignalementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalementServiceTest {

    @Mock SignalementRepository repo;
    @Mock ForetClient foret;
    @Mock EventPublisher publisher;
    @InjectMocks SignalementService service;

    private Signalement signalement(Statut statut) {
        Signalement s = new Signalement();
        s.setId("1");
        s.setAuteurId("u-12");
        s.setSecteurId(14L);
        s.setStatut(statut);
        s.setPosition(new GeoJsonPoint(10.08, 36.42));
        return s;
    }

    @Test
    void creerAvecForetIndisponibleGardeLeFeuEnSecteurAVerifier() {
        when(foret.localiser(anyDouble(), anyDouble())).thenThrow(new RuntimeException("down"));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        var req = new SignalementRequest(TypeSignalement.FUMEE, 36.42, 10.08, 3, "fumée", null);

        var res = service.creer(req, "u-12");

        assertEquals(Statut.SECTEUR_A_VERIFIER, res.signalement().getStatut());
        assertNull(res.signalement().getSecteurId());
    }

    @Test
    void creerAvecForetDisponibleRattacheLeSecteur() {
        when(foret.localiser(anyDouble(), anyDouble())).thenReturn(new SecteurDto(14L, "ZAG-03"));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        var req = new SignalementRequest(TypeSignalement.FEU, 36.42, 10.08, 4, null, null);

        var res = service.creer(req, "u-12");

        assertEquals(Statut.SIGNALE, res.signalement().getStatut());
        assertEquals(14L, res.signalement().getSecteurId());
    }

    @Test
    void confirmerPublieFeuConfirmeEtHistorise() {
        when(repo.findById("1")).thenReturn(Optional.of(signalement(Statut.SIGNALE)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Signalement s = service.confirmer("1", "g-4", "vu sur place");

        assertEquals(Statut.CONFIRME, s.getStatut());
        assertEquals(1, s.getHistorique().size());
        verify(publisher).publierFeuConfirme(s);
    }

    @Test
    void confirmerUnSignalementClosEstRefuse() {
        when(repo.findById("1")).thenReturn(Optional.of(signalement(Statut.CLOS)));
        assertThrows(TransitionInvalideException.class, () -> service.confirmer("1", "g-4", null));
        verifyNoInteractions(publisher);
    }

    @Test
    void cloreApresInterventionEstIdempotent() {
        Signalement s = signalement(Statut.CLOS);
        when(repo.findById("1")).thenReturn(Optional.of(s));
        service.cloreApresIntervention("1", 14L);
        verify(repo, never()).save(any());
    }
}
