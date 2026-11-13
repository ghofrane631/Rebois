package tn.reboistn.coordination.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Alerte envoyée à un utilisateur. Sans incident, c'est une alerte de vigilance. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class AlerteEmise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "incident_id")
    private Incident incident;

    /** Référence vers le destinataire (ms-user). */
    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeAlerte type;

    @Enumerated(EnumType.STRING)
    private Canal canal;

    private boolean lue;

    private LocalDateTime emiseLe;

    /** Expose l'identifiant de l'incident dans le JSON sans renvoyer tout l'objet. */
    public Long getIncidentId() {
        return incident == null ? null : incident.getId();
    }
}
