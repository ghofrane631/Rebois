package tn.reboistn.restauration.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "plan_restauration", uniqueConstraints = @UniqueConstraint(columnNames = "zone_id"))
public class PlanRestauration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "zone_id", nullable = false, unique = true)
    private ZoneSinistree zone;

    @Column(name = "vegetation_cible", length = 120)
    private String vegetationCible;

    @Column(name = "budget_estime", precision = 14, scale = 2, nullable = false)
    private BigDecimal budgetEstime;

    @OneToMany(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<ActionRestauration> actions = new ArrayList<>();

    protected PlanRestauration() {
    }

    public PlanRestauration(String vegetationCible, BigDecimal budgetEstime) {
        this.vegetationCible = vegetationCible;
        this.budgetEstime = budgetEstime;
    }

    public void addAction(ActionRestauration action) {
        actions.add(action);
        action.setPlan(this);
    }

    public Long getId() { return id; }
    public ZoneSinistree getZone() { return zone; }
    public void setZone(ZoneSinistree zone) { this.zone = zone; }
    public String getVegetationCible() { return vegetationCible; }
    public BigDecimal getBudgetEstime() { return budgetEstime; }
    public List<ActionRestauration> getActions() { return actions; }
}