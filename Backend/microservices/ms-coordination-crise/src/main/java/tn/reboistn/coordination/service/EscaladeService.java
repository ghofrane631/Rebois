package tn.reboistn.coordination.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tn.reboistn.coordination.model.*;
import tn.reboistn.coordination.repository.AlerteEmiseRepository;
import tn.reboistn.coordination.repository.EscaladeRepository;
import tn.reboistn.coordination.repository.RegleEscaladeRepository;

import java.time.LocalDateTime;
import java.util.List;

/** Règles d'escalade et escalade automatique des alertes restées non lues. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EscaladeService {

    private final RegleEscaladeRepository regleRepository;
    private final AlerteEmiseRepository alerteRepository;
    private final EscaladeRepository escaladeRepository;

    public List<RegleEscalade> regles() {
        return regleRepository.findAll();
    }

    public RegleEscalade creerRegle(RegleEscalade regle) {
        if (regleRepository.existsByTypeAlerte(regle.getTypeAlerte())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Une regle existe deja pour ce type d'alerte");
        }
        regle.setId(null);
        return regleRepository.save(regle);
    }

    /** Le type d'alerte d'une règle ne change pas : on modifie le délai, la cible et l'obligation de SMS. */
    public RegleEscalade modifierRegle(Long id, RegleEscalade regle) {
        RegleEscalade existante = regleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Regle introuvable"));
        existante.setDelaiMin(regle.getDelaiMin());
        existante.setCible(regle.getCible());
        existante.setSmsObligatoire(regle.isSmsObligatoire());
        return regleRepository.save(existante);
    }

    /** Règles par défaut, créées au démarrage si la table est vide. */
    public void initialiserRegles() {
        if (regleRepository.count() > 0) {
            return;
        }
        regleRepository.saveAll(List.of(
                regle(TypeAlerte.INCIDENT, 10, CibleEscalade.CASERNE_REGIONALE, true),
                regle(TypeAlerte.VIGILANCE, 30, CibleEscalade.DIRECTION_FORETS, false),
                regle(TypeAlerte.MOYENS_INSUFFISANTS, 5, CibleEscalade.CASERNE_REGIONALE, true)));
        log.info("Regles d'escalade par defaut creees");
    }

    /**
     * Toutes les minutes : une alerte non lue après le délai de sa règle est escaladée, une seule fois.
     * Retourne le nombre d'escalades créées.
     */
    @Scheduled(fixedDelayString = "${escalade.intervalle-ms:60000}")
    @Transactional
    public int escaladerAlertesNonLues() {
        int nombre = 0;
        LocalDateTime maintenant = LocalDateTime.now();
        for (AlerteEmise alerte : alerteRepository.findByLueFalse()) {
            if (escaladeRepository.existsByAlerteId(alerte.getId())) {
                continue;
            }
            RegleEscalade regle = regleRepository.findByTypeAlerte(alerte.getType()).orElse(null);
            if (regle == null || alerte.getEmiseLe().plusMinutes(regle.getDelaiMin()).isAfter(maintenant)) {
                continue;
            }
            Escalade escalade = new Escalade();
            escalade.setAlerte(alerte);
            escalade.setRegle(regle);
            escalade.setDeclencheeLe(maintenant);
            escaladeRepository.save(escalade);
            nombre++;
            log.info("Alerte {} non lue apres {} min : escaladee vers {}{}", alerte.getId(), regle.getDelaiMin(),
                    regle.getCible(), regle.isSmsObligatoire() ? " (SMS simule)" : "");
        }
        return nombre;
    }

    private RegleEscalade regle(TypeAlerte type, int delaiMin, CibleEscalade cible, boolean sms) {
        RegleEscalade regle = new RegleEscalade();
        regle.setTypeAlerte(type);
        regle.setDelaiMin(delaiMin);
        regle.setCible(cible);
        regle.setSmsObligatoire(sms);
        return regle;
    }
}
