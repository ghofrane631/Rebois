package tn.reboistn.coordination.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Abonnement d'un utilisateur aux alertes d'une zone, à partir d'un niveau de risque minimum. */
@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "gouvernorat"}))
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String userId;

    @NotBlank
    private String gouvernorat;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Niveau niveauMin;
}
