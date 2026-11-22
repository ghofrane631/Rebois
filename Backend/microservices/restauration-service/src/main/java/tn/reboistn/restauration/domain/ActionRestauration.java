package tn.reboistn.restauration.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "action_restauration")
public class ActionRestauration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private PlanRestauration plan;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private TypeAction type;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false)
    private boolean realisee;

    protected ActionRestauration() {
    }

    public ActionRestauration(TypeAction type, String description) {
        this.type = type;
        this.description = description;
    }

    public Long getId() { return id; }
    public PlanRestauration getPlan() { return plan; }
    public void setPlan(PlanRestauration plan) { this.plan = plan; }
    public TypeAction getType() { return type; }
    public String getDescription() { return description; }
    public boolean isRealisee() { return realisee; }
    public void setRealisee(boolean realisee) { this.realisee = realisee; }
}