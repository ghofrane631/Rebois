package tn.esprit.forestfirewatch.ressources.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Moyen placé à l'avance près d'un secteur passé en risque EXTRÊME.
 * Le moyen reste DISPONIBLE : il est seulement plus proche du danger.
 */
@Entity
@Table(name = "pre_positionnement")
public class PrePositionnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ressource_id", nullable = false)
    private Ressource ressource;

    /** Secteur du service Forêt (référence par identifiant, sans jointure). */
    @Column(name = "secteur_id", nullable = false)
    private Long secteurId;

    @Column(name = "date_positionnement", nullable = false)
    private LocalDateTime date;

    public PrePositionnement() {
    }

    public PrePositionnement(Ressource ressource, Long secteurId) {
        this.ressource = ressource;
        this.secteurId = secteurId;
        this.date = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Ressource getRessource() { return ressource; }
    public Long getSecteurId() { return secteurId; }
    public LocalDateTime getDate() { return date; }
}
