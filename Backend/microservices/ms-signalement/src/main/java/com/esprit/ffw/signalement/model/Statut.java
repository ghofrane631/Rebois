package com.esprit.ffw.signalement.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Statut d'un signalement. SIGNALE : en attente de garde ; "
        + "SECTEUR_A_VERIFIER : ms-foret indisponible ; CONFIRME : feu validé ; "
        + "FAUSSE_ALERTE : classé sans suite ; CLOS : intervention terminée")
public enum Statut { SIGNALE, SECTEUR_A_VERIFIER, CONFIRME, FAUSSE_ALERTE, CLOS }
