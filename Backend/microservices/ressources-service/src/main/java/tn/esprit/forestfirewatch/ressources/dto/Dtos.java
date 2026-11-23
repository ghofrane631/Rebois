package tn.esprit.forestfirewatch.ressources.dto;

import jakarta.validation.constraints.*;
import tn.esprit.forestfirewatch.ressources.entity.*;

import java.time.LocalDateTime;
import java.util.List;

/** Objets échangés par l'API REST (contrats d'API du service Ressources). */
public final class Dtos {

    private Dtos() {
    }

    /** POST /api/ressources : recenser un moyen. */
    public record RessourceRequest(
            @NotNull TypeRessource type,
            @NotNull Long uniteId,
            @NotBlank @Size(max = 30) String gouvernorat,
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude) {
    }

    public record RessourceResponse(Long id, TypeRessource type, Long uniteId, String gouvernorat,
                                    double latitude, double longitude, StatutRessource statut) {
        public static RessourceResponse of(Ressource r) {
            return new RessourceResponse(r.getId(), r.getType(), r.getUniteId(), r.getGouvernorat(),
                    r.getLatitude(), r.getLongitude(), r.getStatut());
        }
    }

    /**
     * POST /api/ressources/reservations, appelé par Coordination de crise (OpenFeign).
     * Exemple : {"incidentId":5, "type":"CAMION_CITERNE", "nombre":2, "lat":36.42, "lng":10.08}
     */
    public record ReservationRequest(
            @NotNull Long incidentId,
            @NotNull TypeRessource type,
            @NotNull @Min(1) @Max(10) Integer nombre,
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double lat,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double lng) {
    }

    /** Réponse 201 : {"incidentId":5, "ressources":[31, 44], "reservations":[...]} */
    public record ReservationResultat(Long incidentId, List<Long> ressources, List<ReservationResponse> reservations) {
    }

    public record ReservationResponse(Long id, Long ressourceId, TypeRessource type, Long incidentId,
                                      LocalDateTime debut, LocalDateTime fin, StatutReservation statut) {
        public static ReservationResponse of(Reservation r) {
            return new ReservationResponse(r.getId(), r.getRessource().getId(), r.getRessource().getType(),
                    r.getIncidentId(), r.getDebut(), r.getFin(), r.getStatut());
        }
    }

    /** PATCH /api/ressources/{id}/statut : passer en maintenance ou revenir disponible. */
    public record StatutRequest(@NotNull StatutRessource statut) {
    }

    /** Corps d'erreur commun : {"error":"AUCUN_MOYEN_DISPONIBLE", "message":"..."} */
    public record ErreurResponse(String error, String message) {
    }
}
