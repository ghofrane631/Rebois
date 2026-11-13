package tn.reboistn.coordination.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Moyen de lutte engagé sur un incident. Son état (disponible, réservé) appartient à ms-ressources. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"incident_id", "ressourceId"}))
public class MoyenEngage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(optional = false)
    @JoinColumn(name = "incident_id")
    private Incident incident;

    /** Référence vers le moyen (ms-ressources). */
    @Column(nullable = false)
    private Long ressourceId;

    @Enumerated(EnumType.STRING)
    private TypeMoyen type;

    /** Référence vers la réservation faite dans ms-ressources. */
    private Long reservationId;

    private LocalDateTime engageLe;
}
