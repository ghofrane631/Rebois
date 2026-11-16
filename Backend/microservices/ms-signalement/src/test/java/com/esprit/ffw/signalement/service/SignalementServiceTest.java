package com.esprit.ffw.signalement.service;

import com.esprit.ffw.signalement.client.ForetGateway;
import com.esprit.ffw.signalement.client.SecteurDto;
import com.esprit.ffw.signalement.dto.SignalementRequest;
import com.esprit.ffw.signalement.event.EventPublisher;
import com.esprit.ffw.signalement.exception.TransitionInvalideException;
import com.esprit.ffw.signalement.model.HistoriqueStatut;
import com.esprit.ffw.signalement.model.Photo;
import com.esprit.ffw.signalement.model.Signalement;
import com.esprit.ffw.signalement.model.Statut;
import com.esprit.ffw.signalement.model.TypeSignalement;
import com.esprit.ffw.signalement.repository.SignalementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalementServiceTest {

    @Mock SignalementRepository repo;
    @Mock ForetGateway foret;
    @Mock EventPublisher publisher;
    @InjectMocks SignalementService service;

    private Signalement signalement(Statut statut) {
        Signalement s = new Signalement();
        s.setId("1");
        s.setAuteurId("u-12");
        s.setSecteurId(14L);
        s.setStatut(statut);
        s.setPosition(new GeoJsonPoint(10.08, 36.42));
        s.setCreeLe(Instant.now());
        return s;
    }

    private void aucunVoisin() {
        when(repo.findByPositionNearAndCreeLeAfter(any(), any(), any())).thenReturn(List.of());
    }

    @Test
    void creerAvecForetIndisponibleGardeLeFeuEnSecteurAVerifier() {
        aucunVoisin();
        when(foret.localiser(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        var req = new SignalementRequest(TypeSignalement.FUMEE, 36.42, 10.08, 3, "fumée", null);

        var res = service.creer(req, "u-12");

        assertEquals(Statut.SECTEUR_A_VERIFIER, res.signalement().getStatut());
        assertNull(res.signalement().getSecteurId());
        assertNotNull(res.signalement().getCreeLe());
    }

    @Test
    void creerAvecForetDisponibleRattacheLeSecteur() {
        aucunVoisin();
        when(foret.localiser(anyDouble(), anyDouble())).thenReturn(Optional.of(new SecteurDto(14L, "ZAG-03")));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        var req = new SignalementRequest(TypeSignalement.FEU, 36.42, 10.08, 4, null, null);

        var res = service.creer(req, "u-12");

        assertEquals(Statut.SIGNALE, res.signalement().getStatut());
        assertEquals(14L, res.signalement().getSecteurId());
    }

    @Test
    void creerRetourneLesIdentifiantsDesDoublonsProches() {
        Signalement existant = signalement(Statut.SIGNALE);
        when(repo.findByPositionNearAndCreeLeAfter(any(), any(), any())).thenReturn(List.of(existant));
        when(foret.localiser(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        var req = new SignalementRequest(TypeSignalement.BRAISES, 36.421, 10.081, 2, null, null);

        var res = service.creer(req, "u-12");

        assertEquals(List.of("1"), res.doublonsProches());
    }

    @Test
    void creerIgnoreUnDoublonNonActif() {
        when(repo.findByPositionNearAndCreeLeAfter(any(), any(), any()))
                .thenReturn(List.of(signalement(Statut.FAUSSE_ALERTE)));
        when(foret.localiser(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
        var req = new SignalementRequest(TypeSignalement.FUMEE, 36.42, 10.08, 3, null, null);

        assertTrue(service.creer(req, "u-12").doublonsProches().isEmpty());
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
        verify(repo, never()).save(any());
        verifyNoInteractions(publisher);
    }

    @Test
    void fausseAlerteNePublieAucunEvenement() {
        when(repo.findById("1")).thenReturn(Optional.of(signalement(Statut.SIGNALE)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Signalement s = service.fausseAlerte("1", "g-4", "nuage de poussière");

        assertEquals(Statut.FAUSSE_ALERTE, s.getStatut());
        verifyNoInteractions(publisher);
    }

    @Test
    void ajouterPhotoRefuseUnAutreUtilisateur() {
        when(repo.findById("1")).thenReturn(Optional.of(signalement(Statut.SIGNALE)));

        assertThrows(AccessDeniedException.class,
                () -> service.ajouterPhoto("1", "https://x/p.jpg", "intrus"));
        verify(repo, never()).save(any());
    }

    @Test
    void ajouterPhotoLimiteLeNombreDePhotos() {
        Signalement s = signalement(Statut.SIGNALE);
        for (int i = 0; i < SignalementService.MAX_PHOTOS; i++) {
            s.getPhotos().add(new Photo("https://x/" + i + ".jpg", Instant.now()));
        }
        when(repo.findById("1")).thenReturn(Optional.of(s));

        assertThrows(IllegalArgumentException.class,
                () -> service.ajouterPhoto("1", "https://x/extra.jpg", "u-12"));
    }

    @Test
    void ajouterPhotoNettoieLUrl() {
        when(repo.findById("1")).thenReturn(Optional.of(signalement(Statut.SIGNALE)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Signalement s = service.ajouterPhoto("1", "  https://x/p.jpg  ", "u-12");

        assertEquals("https://x/p.jpg", s.getPhotos().get(0).getUrl());
    }

    @Test
    void rechercherUtiliseLaPagination() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repo.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        var page = service.rechercher(null, null, pageable);

        assertEquals(0, page.getTotalElements());
        verify(repo).findAll(pageable);
    }

    @Test
    void cloreApresInterventionEstIdempotent() {
        Signalement s = signalement(Statut.CLOS);
        when(repo.findById("1")).thenReturn(Optional.of(s));
        service.cloreApresIntervention("1", 14L);
        verify(repo, never()).save(any());
    }

    @Test
    void cloreApresInterventionClotTousLesFeuxConfirmesDuSecteur() {
        Signalement a = signalement(Statut.CONFIRME);
        Signalement b = signalement(Statut.CONFIRME);
        when(repo.findBySecteurIdAndStatut(14L, Statut.CONFIRME)).thenReturn(List.of(a, b));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        service.cloreApresIntervention(null, 14L);

        assertEquals(Statut.CLOS, a.getStatut());
        assertEquals(Statut.CLOS, b.getStatut());
        assertEquals(1, a.getHistorique().size());
        assertEquals("system", a.getHistorique().get(0).getGardeId());
        verify(repo, times(2)).save(any());
    }

    @Test
    void cloreApresInterventionSansCibleNeFaitRien() {
        service.cloreApresIntervention(null, null);
        verifyNoInteractions(repo);
    }

    @Test
    void historiqueContientLAncienEtLeNouveauStatut() {
        when(repo.findById("1")).thenReturn(Optional.of(signalement(Statut.SECTEUR_A_VERIFIER)));
        when(repo.save(any())).thenAnswer(i -> i.getArgument(0));

        Signalement s = service.confirmer("1", "g-4", "vérifié");

        HistoriqueStatut h = s.getHistorique().get(0);
        assertEquals(Statut.SECTEUR_A_VERIFIER, h.getAncien());
        assertEquals(Statut.CONFIRME, h.getNouveau());
        assertEquals("g-4", h.getGardeId());
        assertNotNull(h.getDate());
    }
}
