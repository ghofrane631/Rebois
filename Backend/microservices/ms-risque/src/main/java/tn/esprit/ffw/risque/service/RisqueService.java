package tn.esprit.ffw.risque.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tn.esprit.ffw.risque.client.ForetClient;
import tn.esprit.ffw.risque.dto.*;
import tn.esprit.ffw.risque.entity.*;
import tn.esprit.ffw.risque.events.*;
import tn.esprit.ffw.risque.repository.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class RisqueService {

    private static final Logger log = LoggerFactory.getLogger(RisqueService.class);

    private final ReleveMeteoRepository releveRepo;
    private final IndiceSecteurRepository indiceRepo;
    private final HistoriqueNiveauRepository historiqueRepo;
    private final FeuRecentRepository feuRepo;
    private final SeuilRisqueRepository seuilRepo;
    private final EvenementTraiteRepository evenementRepo;
    private final CalculIndiceService calcul;
    private final ForetClient foretClient;
    private final ApplicationEventPublisher publisher;
    private final TransactionTemplate tx;

    // =====================================================================
    //  SYNCHRONE : POST /releves  (Feign -> Forêt, puis calcul + sauvegarde)
    // =====================================================================
    public ReleveResponse enregistrerReleve(ReleveRequest req, String garde) {
        // 1) Appel synchrone HORS transaction : 404 -> SecteurIntrouvable, panne -> 503
        foretClient.getSecteur(req.secteurId());

        // 2) Transaction locale : relevé + indice + historique (+ événement après commit)
        return tx.execute(status -> {
            ReleveMeteo r = new ReleveMeteo();
            r.setSecteurId(req.secteurId());
            r.setDateReleve(req.dateReleve() != null ? req.dateReleve() : LocalDateTime.now());
            r.setTemperature(req.temperature());
            r.setHumidite(req.humidite());
            r.setVent(req.vent());
            r.setJoursSansPluie(req.joursSansPluie());
            r.setSaisiPar(garde);
            releveRepo.save(r);

            IndiceSecteur idx = indiceOuNouveau(req.secteurId());
            idx.setScoreMeteo(calcul.scoreMeteo(r.getTemperature(), r.getHumidite(), r.getVent(), r.getJoursSansPluie()));
            recalculer(idx, "RELEVE_METEO");

            return new ReleveResponse(r.getId(), r.getSecteurId(), r.getDateReleve(), r.getTemperature(),
                    r.getHumidite(), r.getVent(), r.getJoursSansPluie(), idx.getIndice(), idx.getNiveau());
        });
    }

    // =====================================================================
    //  ASYNCHRONE : traitement des événements consommés (idempotent)
    // =====================================================================
    @Transactional
    public void traiter(SecteurCree e) {
        if (dejaTraite(e.eventId(), "SecteurCree")) return;
        if (!indiceRepo.existsById(e.secteurId())) {
            IndiceSecteur idx = indiceOuNouveau(e.secteurId());
            idx.setIndice(0);
            idx.setNiveau(NiveauRisque.FAIBLE);
            idx.setMajLe(LocalDateTime.now());
            indiceRepo.save(idx);
        }
    }

    @Transactional
    public void traiter(FeuConfirme e) {
        if (dejaTraite(e.eventId(), "FeuConfirme")) return;
        FeuRecent f = feuRepo.findById(e.feuId()).orElseGet(FeuRecent::new);
        f.setFeuId(e.feuId());
        f.setSecteurId(e.secteurId());
        f.setDateConfirmation(e.dateConfirmation() != null ? e.dateConfirmation() : LocalDateTime.now());
        f.setEteint(false);
        feuRepo.save(f);
        recalculer(indiceOuNouveau(e.secteurId()), "FEU_CONFIRME");
    }

    @Transactional
    public void traiter(InterventionTerminee e) {
        if (dejaTraite(e.eventId(), "InterventionTerminee")) return;
        feuRepo.findById(e.feuId()).ifPresent(f -> { f.setEteint(true); feuRepo.save(f); });
        recalculer(indiceOuNouveau(e.secteurId()), "INTERVENTION_TERMINEE");
    }

    @Transactional
    public void traiter(ZoneRestauree e) {
        if (dejaTraite(e.eventId(), "ZoneRestauree")) return;
        IndiceSecteur idx = indiceOuNouveau(e.secteurId());
        idx.setAjustement(Math.max(CalculIndiceService.AJUSTEMENT_MIN,
                idx.getAjustement() + CalculIndiceService.AJUSTEMENT_RESTAURATION));
        recalculer(idx, "ZONE_RESTAUREE");
    }

    // =====================================================================
    //  Lectures
    // =====================================================================
    @Transactional(readOnly = true)
    public IndiceResponse indiceSecteur(Long secteurId) {
        return indiceRepo.findById(secteurId).map(this::versReponse)
                .orElseThrow(() -> new tn.esprit.ffw.risque.exception.SecteurIntrouvableException(secteurId));
    }

    @Transactional(readOnly = true)
    public List<IndiceResponse> carte(NiveauRisque niveau) {
        List<IndiceSecteur> l = niveau == null ? indiceRepo.findAll() : indiceRepo.findByNiveau(niveau);
        return l.stream().map(this::versReponse).toList();
    }

    @Transactional(readOnly = true)
    public List<ReleveResponse> releves(Long secteurId, LocalDateTime from, LocalDateTime to) {
        LocalDateTime debut = from != null ? from : LocalDateTime.now().minusDays(7);
        LocalDateTime fin = to != null ? to : LocalDateTime.now();
        return releveRepo.rechercher(secteurId, debut, fin).stream()
                .map(r -> {
                    IndiceSecteur i = indiceRepo.findById(r.getSecteurId()).orElse(null);
                    return new ReleveResponse(r.getId(), r.getSecteurId(), r.getDateReleve(), r.getTemperature(),
                            r.getHumidite(), r.getVent(), r.getJoursSansPluie(),
                            i != null ? i.getIndice() : 0, i != null ? i.getNiveau() : NiveauRisque.FAIBLE);
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<HistoriqueResponse> historique(Long secteurId) {
        return historiqueRepo.findBySecteurIdOrderByDateDesc(secteurId).stream()
                .map(h -> new HistoriqueResponse(h.getSecteurId(), h.getAncien(), h.getNouveau(),
                        h.getIndice(), h.getCause(), h.getDate())).toList();
    }

    // =====================================================================
    //  Seuils (ADMIN)
    // =====================================================================
    @Transactional
    public List<SeuilDto> mettreAJourSeuils(List<SeuilDto> seuils) {
        Map<NiveauRisque, Integer> m = new EnumMap<>(NiveauRisque.class);
        seuils.forEach(s -> m.put(s.niveau(), s.valeurMin()));
        if (m.size() != NiveauRisque.values().length) {
            throw new IllegalArgumentException("Les 5 niveaux doivent être fournis");
        }
        if (m.get(NiveauRisque.FAIBLE) != 0) {
            throw new IllegalArgumentException("Le seuil de FAIBLE doit valoir 0");
        }
        int precedent = -1;
        for (NiveauRisque n : NiveauRisque.values()) {
            if (m.get(n) <= precedent) {
                throw new IllegalArgumentException("Les seuils doivent être strictement croissants");
            }
            precedent = m.get(n);
        }
        m.forEach((n, v) -> seuilRepo.save(new SeuilRisque(n, v)));
        return seuilRepo.findAll().stream()
                .sorted(Comparator.comparingInt(SeuilRisque::getValeurMin))
                .map(s -> new SeuilDto(s.getNiveau(), s.getValeurMin())).toList();
    }

    // =====================================================================
    //  Interne
    // =====================================================================
    private void recalculer(IndiceSecteur idx, String cause) {
        LocalDateTime depuis = LocalDateTime.now().minusDays(CalculIndiceService.FENETRE_FEUX_JOURS);
        idx.setFeuxRecents(feuRepo.countBySecteurIdAndEteintFalseAndDateConfirmationAfter(idx.getSecteurId(), depuis));
        idx.setIndice(calcul.indice(idx.getScoreMeteo(), idx.getFeuxRecents(), idx.getAjustement()));

        NiveauRisque ancien = idx.getNiveau();
        NiveauRisque nouveau = niveauPour(idx.getIndice());
        idx.setNiveau(nouveau);
        idx.setMajLe(LocalDateTime.now());
        indiceRepo.save(idx);

        if (ancien != nouveau) {
            HistoriqueNiveau h = new HistoriqueNiveau();
            h.setSecteurId(idx.getSecteurId());
            h.setAncien(ancien);
            h.setNouveau(nouveau);
            h.setIndice(idx.getIndice());
            h.setCause(cause);
            h.setDate(LocalDateTime.now());
            historiqueRepo.save(h);

            log.info("Secteur {} : {} -> {} (indice {}, cause {})", idx.getSecteurId(), ancien, nouveau, idx.getIndice(), cause);
            // Publié vers RabbitMQ uniquement APRÈS commit (voir NiveauRisquePublisher)
            publisher.publishEvent(new NiveauRisqueChange(UUID.randomUUID().toString(), idx.getSecteurId(),
                    ancien, nouveau, idx.getIndice(), cause, LocalDateTime.now()));
        }
    }

    NiveauRisque niveauPour(int indice) {
        return seuilRepo.findAll().stream()
                .sorted(Comparator.comparingInt(SeuilRisque::getValeurMin).reversed())
                .filter(s -> indice >= s.getValeurMin())
                .map(SeuilRisque::getNiveau)
                .findFirst().orElse(NiveauRisque.FAIBLE);
    }

    private IndiceSecteur indiceOuNouveau(Long secteurId) {
        return indiceRepo.findById(secteurId).orElseGet(() -> {
            IndiceSecteur i = new IndiceSecteur();
            i.setSecteurId(secteurId);
            return i;
        });
    }

    private boolean dejaTraite(String eventId, String type) {
        if (eventId == null || eventId.isBlank()) {
            throw new IllegalArgumentException("eventId manquant sur " + type);
        }
        if (evenementRepo.existsById(eventId)) {
            log.info("{} {} déjà traité, ignoré (idempotence)", type, eventId);
            return true;
        }
        evenementRepo.save(new EvenementTraite(eventId, type));
        return false;
    }

    private IndiceResponse versReponse(IndiceSecteur i) {
        return new IndiceResponse(i.getSecteurId(), i.getIndice(), i.getNiveau(), i.getFeuxRecents(), i.getMajLe());
    }
}
