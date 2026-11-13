package tn.reboistn.coordination.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Une ligne de la chronologie d'un incident. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class EvenementChrono {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    private LocalDateTime heure;

    @Column(nullable = false)
    private String action;

    @Enumerated(EnumType.STRING)
    private SourceAction source;
}
