package tn.reboistn.restauration.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "zone_sinistree", uniqueConstraints = @UniqueConstraint(columnNames = "incident_id"))
public class ZoneSinistree {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "incident_id", nullable = false, unique = true)
    private Long incidentId;

    @Column(name = "secteur_id", nullable = false)
    private Long secteurId;

    @Column(name = "surface_brulee_ha", precision = 10, scale = 2, nullable = false)
    private BigDecimal surfaceBruleeHa;

    @Column(length = 80)
    private String gouvernorat;

    @Enumerated(EnumType.STRING)
    @Column(length = 16)
    private Gravite gravite;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutZone statut = StatutZone.A_EVALUER;

    @OneToOne(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    private EvaluationDegats evaluation;

    @OneToOne(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    private PlanRestauration plan;

    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dateSuivi DESC")
    private List<SuiviRepousse> suivis = new ArrayList<>();

    protected ZoneSinistree() {
    }

    public ZoneSinistree(Long incidentId, Long secteurId, BigDecimal surfaceBruleeHa, String gouvernorat) {
        this.incidentId = incidentId;
        this.secteurId = secteurId;
        this.surfaceBruleeHa = surfaceBruleeHa;
        this.gouvernorat = gouvernorat;
    }

    public void setEvaluation(EvaluationDegats evaluation) {
        this.evaluation = evaluation;
        evaluation.setZone(this);
        if (evaluation.getGravite() != null) {
            this.gravite = evaluation.getGravite();
        }
        this.statut = StatutZone.EVALUEE;
    }

    public void setPlan(PlanRestauration plan) {
        this.plan = plan;
        plan.setZone(this);
        this.statut = StatutZone.EN_RESTAURATION;
    }

    public void addSuivi(SuiviRepousse suivi) {
        suivis.add(0, suivi);
        suivi.setZone(this);
    }

    public Long getId() { return id; }
    public Long getIncidentId() { return incidentId; }
    public Long getSecteurId() { return secteurId; }
    public BigDecimal getSurfaceBruleeHa() { return surfaceBruleeHa; }
    public String getGouvernorat() { return gouvernorat; }
    public Gravite getGravite() { return gravite; }
    public StatutZone getStatut() { return statut; }
    public EvaluationDegats getEvaluation() { return evaluation; }
    public PlanRestauration getPlan() { return plan; }
    public List<SuiviRepousse> getSuivis() { return suivis; }
    public void setStatut(StatutZone statut) { this.statut = statut; }
}