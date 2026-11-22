package tn.reboistn.restauration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "evaluation_degats", uniqueConstraints = @UniqueConstraint(columnNames = "zone_id"))
public class EvaluationDegats {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "zone_id", nullable = false, unique = true)
    private ZoneSinistree zone;

    @Column(nullable = false)
    private int arbresPerdus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private EtatSol etatSol;

    @Column(nullable = false)
    private boolean risqueErosion;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Gravite gravite;

    @Column(nullable = false)
    private LocalDateTime evalueeLe;

    protected EvaluationDegats() {
    }

    public EvaluationDegats(int arbresPerdus, EtatSol etatSol, boolean risqueErosion, Gravite gravite) {
        this.arbresPerdus = arbresPerdus;
        this.etatSol = etatSol;
        this.risqueErosion = risqueErosion;
        this.gravite = gravite;
        this.evalueeLe = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public ZoneSinistree getZone() { return zone; }
    public void setZone(ZoneSinistree zone) { this.zone = zone; }
    public int getArbresPerdus() { return arbresPerdus; }
    public EtatSol getEtatSol() { return etatSol; }
    public boolean isRisqueErosion() { return risqueErosion; }
    public Gravite getGravite() { return gravite; }
    public LocalDateTime getEvalueeLe() { return evalueeLe; }
}