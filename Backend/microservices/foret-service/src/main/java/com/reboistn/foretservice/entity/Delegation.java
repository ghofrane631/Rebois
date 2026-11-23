package com.reboistn.foretservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "delegations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delegation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id", nullable = false)
    private Region region;
}
