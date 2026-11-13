package tn.reboistn.coordination.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Règle : après delaiMin minutes sans lecture, une alerte de ce type est escaladée vers la cible. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class RegleEscalade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private TypeAlerte typeAlerte;

    @NotNull
    @Positive
    private Integer delaiMin;

    @NotNull
    @Enumerated(EnumType.STRING)
    private CibleEscalade cible;

    private boolean smsObligatoire;
}
