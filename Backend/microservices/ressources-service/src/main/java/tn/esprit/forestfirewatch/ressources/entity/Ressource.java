package tn.esprit.forestfirewatch.ressources.entity;

import jakarta.persistence.*;

/**
 * Un moyen de lutte (camion, équipe, hélicoptère, bulldozer) rattaché à une caserne.
 * La colonne version active le verrouillage optimiste : deux réservations
 * simultanées du même moyen ne peuvent pas réussir toutes les deux.
 */
@Entity
@Table(name = "ressource", indexes = {
        @Index(name = "idx_type_statut_gouvernorat", columnList = "type, statut, gouvernorat")
})
public class Ressource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TypeRessource type;

    /** Caserne propriétaire (identifiant d'une Unite du service User). */
    @Column(name = "unite_id", nullable = false)
    private Long uniteId;

    @Column(nullable = false, length = 30)
    private String gouvernorat;

    /** Position de la caserne, pour choisir les moyens les plus proches du feu. */
    @Column(nullable = false)
    private double latitude;

    @Column(nullable = false)
    private double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StatutRessource statut = StatutRessource.DISPONIBLE;

    @Version
    private int version;

    public Ressource() {
    }

    public Ressource(TypeRessource type, Long uniteId, String gouvernorat, double latitude, double longitude) {
        this.type = type;
        this.uniteId = uniteId;
        this.gouvernorat = gouvernorat;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() { return id; }
    public TypeRessource getType() { return type; }
    public void setType(TypeRessource type) { this.type = type; }
    public Long getUniteId() { return uniteId; }
    public void setUniteId(Long uniteId) { this.uniteId = uniteId; }
    public String getGouvernorat() { return gouvernorat; }
    public void setGouvernorat(String gouvernorat) { this.gouvernorat = gouvernorat; }
    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public StatutRessource getStatut() { return statut; }
    public void setStatut(StatutRessource statut) { this.statut = statut; }
    public int getVersion() { return version; }
}
