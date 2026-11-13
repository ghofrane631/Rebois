package tn.reboistn.coordination.dto;

import tn.reboistn.coordination.model.*;

import java.util.List;

/** Réponse de GET /api/incidents/{id} : l'incident avec sa chronologie, ses moyens, ses alertes et son bilan. */
public record IncidentDetail(
        Incident incident,
        List<EvenementChrono> chronologie,
        List<MoyenEngage> moyens,
        List<AlerteEmise> alertes,
        Bilan bilan) {
}
