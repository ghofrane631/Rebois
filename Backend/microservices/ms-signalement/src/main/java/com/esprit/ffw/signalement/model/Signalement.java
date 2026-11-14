package com.esprit.ffw.signalement.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor
@Document(collection = "signalements")
public class Signalement {
    @Id
    private String id;
    private String auteurId;             // sub du JWT Keycloak
    private Long secteurId;              // peut être vide (SECTEUR_A_VERIFIER)
    private TypeSignalement type;
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint position;       // GeoJSON [longitude, latitude]
    private int intensite;               // 1 à 5
    private String description;
    private Statut statut;
    private List<Photo> photos = new ArrayList<>();
    private List<HistoriqueStatut> historique = new ArrayList<>();
    private LocalDateTime creeLe;
}
