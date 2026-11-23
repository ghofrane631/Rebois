package tn.esprit.forestfirewatch.ressources.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Réservation d'un moyen pour un incident de Coordination de crise.
 * On ne garde que l'identifiant de l'incident : ses données appartiennent à Coordination de crise.
 */
@Entity
@Table(name = "reservation", indexes = {
        @Index(name = "idx_incident_statut", columnList = "incident_id, statut")
})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "ressource_id", nullable = false)
    private Ressource ressource;

    @Column(name = "incident_id", nullable = false)
    private Long incidentId;

    @Column(nullable = false)
    private LocalDateTime debut;

    private LocalDateTime fin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StatutReservation statut = StatutReservation.ACTIVE;

    public Reservation() {
    }

    public Reservation(Ressource ressource, Long incidentId) {
        this.ressource = ressource;
        this.incidentId = incidentId;
        this.debut = LocalDateTime.now();
    }

    /** Libère la réservation et rend le moyen disponible. */
    public void liberer() {
        this.statut = StatutReservation.LIBEREE;
        this.fin = LocalDateTime.now();
        this.ressource.setStatut(StatutRessource.DISPONIBLE);
    }

    public Long getId() { return id; }
    public Ressource getRessource() { return ressource; }
    public Long getIncidentId() { return incidentId; }
    public LocalDateTime getDebut() { return debut; }
    public LocalDateTime getFin() { return fin; }
    public StatutReservation getStatut() { return statut; }
}
