package tn.reboistn.coordination.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** Escalade d'une alerte restée non lue : une seule escalade par alerte. */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Escalade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(optional = false)
    @JoinColumn(name = "alerte_id", unique = true)
    private AlerteEmise alerte;

    @ManyToOne(optional = false)
    @JoinColumn(name = "regle_id")
    private RegleEscalade regle;

    private LocalDateTime declencheeLe;

    /** Référence vers l'unité prévenue (ms-user). */
    private Long uniteCibleId;
}
