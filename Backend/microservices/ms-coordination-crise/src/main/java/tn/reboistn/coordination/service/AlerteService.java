package tn.reboistn.coordination.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import tn.reboistn.coordination.client.UserClient;
import tn.reboistn.coordination.model.*;
import tn.reboistn.coordination.repository.AbonnementRepository;
import tn.reboistn.coordination.repository.AlerteEmiseRepository;
import tn.reboistn.coordination.repository.EscaladeRepository;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Alertes ciblées par zone et niveau de risque, et abonnements des utilisateurs. */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlerteService {

    private final AlerteEmiseRepository alerteRepository;
    private final AbonnementRepository abonnementRepository;
    private final EscaladeRepository escaladeRepository;
    private final UserClient userClient;

    /**
     * Alerte la zone d'un incident : les abonnés dont le niveau minimum est atteint,
     * plus les utilisateurs de la zone connus de ms-user. Si ms-user est indisponible,
     * seuls les abonnés locaux sont prévenus.
     */
    public void alerterZone(Incident incident) {
        Set<String> destinataires = abonnesConcernes(incident.getGouvernorat(), incident.getNiveauRisque());
        try {
            userClient.parZone(incident.getGouvernorat()).forEach(u -> destinataires.add(u.id()));
        } catch (RuntimeException e) {
            log.warn("User indisponible : alertes envoyees aux abonnes locaux uniquement ({})", incident.getGouvernorat());
        }
        Canal canal = canalPour(incident.getNiveauRisque());
        destinataires.forEach(userId -> emettre(incident, userId, TypeAlerte.INCIDENT, canal));
    }

    /** Alerte de vigilance quand le risque d'une zone monte (sans incident). */
    public void alerterVigilance(String gouvernorat, Niveau niveau) {
        Canal canal = canalPour(niveau);
        abonnesConcernes(gouvernorat, niveau).forEach(userId -> emettre(null, userId, TypeAlerte.VIGILANCE, canal));
    }

    /** Alerte régionale quand Ressources n'a plus de moyens libres : toujours par SMS. */
    public void alerterMoyensInsuffisants(Incident incident, String gouvernorat) {
        abonnementRepository.findByGouvernorat(gouvernorat)
                .forEach(a -> emettre(incident, a.getUserId(), TypeAlerte.MOYENS_INSUFFISANTS, Canal.SMS));
    }

    public List<AlerteEmise> mesAlertes(String userId, Boolean lue) {
        return lue == null ? alerteRepository.findByUserId(userId) : alerteRepository.findByUserIdAndLue(userId, lue);
    }

    public AlerteEmise marquerLue(Long id) {
        AlerteEmise alerte = trouver(id);
        if (alerte.isLue()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Alerte deja lue");
        }
        alerte.setLue(true);
        return alerteRepository.save(alerte);
    }

    public List<Escalade> escalades(Long alerteId) {
        trouver(alerteId);
        return escaladeRepository.findByAlerteId(alerteId);
    }

    public List<Abonnement> abonnements(String userId) {
        return userId == null ? abonnementRepository.findAll() : abonnementRepository.findByUserId(userId);
    }

    public Abonnement abonner(Abonnement abonnement) {
        if (abonnementRepository.existsByUserIdAndGouvernorat(abonnement.getUserId(), abonnement.getGouvernorat())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Deja abonne a cette zone");
        }
        abonnement.setId(null);
        return abonnementRepository.save(abonnement);
    }

    /** Abonnement créé automatiquement pour un nouvel utilisateur (événement UtilisateurCree). */
    public void abonnementParDefaut(String userId, String gouvernorat) {
        if (abonnementRepository.existsByUserIdAndGouvernorat(userId, gouvernorat)) {
            return;
        }
        Abonnement abonnement = new Abonnement();
        abonnement.setUserId(userId);
        abonnement.setGouvernorat(gouvernorat);
        abonnement.setNiveauMin(Niveau.ELEVE);
        abonnementRepository.save(abonnement);
    }

    public void desabonner(Long id) {
        if (!abonnementRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Abonnement introuvable");
        }
        abonnementRepository.deleteById(id);
    }

    // ---------- méthodes internes ----------

    private Set<String> abonnesConcernes(String gouvernorat, Niveau niveau) {
        Set<String> destinataires = new LinkedHashSet<>();
        abonnementRepository.findByGouvernorat(gouvernorat).stream()
                .filter(a -> niveau.compareTo(a.getNiveauMin()) >= 0)
                .forEach(a -> destinataires.add(a.getUserId()));
        return destinataires;
    }

    /** En risque EXTREME, le SMS est obligatoire. */
    private Canal canalPour(Niveau niveau) {
        return niveau == Niveau.EXTREME ? Canal.SMS : Canal.APP;
    }

    private void emettre(Incident incident, String userId, TypeAlerte type, Canal canal) {
        AlerteEmise alerte = new AlerteEmise();
        alerte.setIncident(incident);
        alerte.setUserId(userId);
        alerte.setType(type);
        alerte.setCanal(canal);
        alerte.setEmiseLe(LocalDateTime.now());
        alerteRepository.save(alerte);
        if (canal == Canal.SMS) {
            // envoi simulé : aucun fournisseur SMS n'est branché pour l'instant
            log.info("SMS simule vers l'utilisateur {} ({})", userId, type);
        }
    }

    private AlerteEmise trouver(Long id) {
        return alerteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alerte introuvable"));
    }
}
