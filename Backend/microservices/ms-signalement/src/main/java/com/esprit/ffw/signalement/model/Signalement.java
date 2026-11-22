package com.esprit.ffw.signalement.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor
@Document(collection = "signalements")
@CompoundIndex(name = "secteur_statut", def = "{'secteurId': 1, 'statut': 1}")
@Schema(name = "Signalement", description = "Signalement d'un départ de feu et son cycle de vie")
public class Signalement {
    @Id
    @Schema(description = "Identifiant MongoDB", example = "66f1c2a8e4b0a1f2c3d4e5f6")
    private String id;

    @Schema(description = "Subject Keycloak de l'auteur", example = "benevole-12")
    private String auteurId;

    @Schema(description = "Secteur forestier rattaché (null si ms-foret était indisponible)", example = "14")
    private Long secteurId;

    @Schema(description = "Type observé", example = "FUMEE")
    private TypeSignalement type;

    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    @Schema(description = "Position GeoJSON [longitude, latitude]")
    private GeoJsonPoint position;       // GeoJSON [longitude, latitude]

    @Schema(description = "Intensité perçue de 1 à 5", example = "3", minimum = "1", maximum = "5")
    private int intensite;

    @Schema(description = "Description libre", example = "Fumée épaisse au-dessus de la pinède")
    private String description;

    @Indexed
    @Schema(description = "Statut courant", example = "CONFIRME")
    private Statut statut;

    @Schema(description = "Photos rattachées (10 maximum)")
    private List<Photo> photos = new ArrayList<>();

    @Schema(description = "Historique des transitions de statut")
    private List<HistoriqueStatut> historique = new ArrayList<>();

    @Indexed
    @Schema(description = "Date de création (UTC)", example = "2026-11-16T18:42:00Z")
    private Instant creeLe;

    /** Verrou optimiste : empêche deux confirmations concurrentes de publier deux fois. */
    @Version
    @Schema(description = "Version pour le verrou optimiste", example = "0")
    private Long version;
}
