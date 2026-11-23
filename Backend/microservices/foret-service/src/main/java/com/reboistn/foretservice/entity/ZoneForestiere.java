package com.reboistn.foretservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.locationtech.jts.geom.Polygon;

import java.time.LocalDateTime;

@Entity
@Table(name = "zones_forestieres")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZoneForestiere {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeZone type;

    @Column(name = "superficie_ha", nullable = false)
    private Double superficieHa;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "geometry(Polygon,4326)")
    private Polygon geometrie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatutZone statut = StatutZone.ACTIVE;

    @CreationTimestamp
    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delegation_id", nullable = false)
    private Delegation delegation;
}
