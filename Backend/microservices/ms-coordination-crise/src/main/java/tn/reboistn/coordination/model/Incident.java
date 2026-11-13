package tn.reboistn.coordination.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Un incident = un feu confirmé, géré de bout en bout par ce service.
 * Les autres objets (signalement, secteur) sont référencés par leur identifiant.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Référence vers le signalement confirmé (ms-signalement) : un seul incident par feu. */
    @NotNull
    @Column(unique = true, nullable = false)
    private Long signalementId;

    /** Référence vers le secteur touché (ms-foret). */
    @NotNull
    private Long secteurId;

    /** Position du feu, utilisée pour réserver les moyens les plus proches. */
    private Double latitude;

    private Double longitude;

    /** Zone à alerter. */
    @NotBlank
    private String gouvernorat;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Niveau niveauRisque;

    private LocalDateTime ouvertLe;

    @Enumerated(EnumType.STRING)
    private StatutIncident statut;
}
