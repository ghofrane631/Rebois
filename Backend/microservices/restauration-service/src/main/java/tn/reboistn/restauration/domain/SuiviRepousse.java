package tn.reboistn.restauration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "suivi_repousse")
public class SuiviRepousse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private ZoneSinistree zone;

    @Column(name = "date_suivi", nullable = false)
    private LocalDate dateSuivi;

    @Column(name = "couverture_pct", nullable = false)
    private int couverturePct;

    @Column(name = "garde_id", nullable = false, length = 64)
    private String gardeId;

    protected SuiviRepousse() {
    }

    public SuiviRepousse(LocalDate dateSuivi, int couverturePct, String gardeId) {
        this.dateSuivi = dateSuivi;
        this.couverturePct = couverturePct;
        this.gardeId = gardeId;
    }

    public Long getId() { return id; }
    public ZoneSinistree getZone() { return zone; }
    public void setZone(ZoneSinistree zone) { this.zone = zone; }
    public LocalDate getDateSuivi() { return dateSuivi; }
    public int getCouverturePct() { return couverturePct; }
    public String getGardeId() { return gardeId; }
}