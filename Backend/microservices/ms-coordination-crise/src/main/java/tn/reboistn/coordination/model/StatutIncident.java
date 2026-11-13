package tn.reboistn.coordination.model;

import java.util.Set;

/** Cycle de vie d'un incident : OUVERT -> EN_COURS (ou MOYENS_A_AFFECTER) -> MAITRISE -> CLOS. */
public enum StatutIncident {
    OUVERT, MOYENS_A_AFFECTER, EN_COURS, MAITRISE, CLOS;

    /** Transitions permises par PATCH /statut. CLOS n'y figure pas : la clôture passe par le bilan. */
    public boolean peutPasserA(StatutIncident cible) {
        return switch (this) {
            case OUVERT -> Set.of(MOYENS_A_AFFECTER, EN_COURS).contains(cible);
            case MOYENS_A_AFFECTER -> cible == EN_COURS;
            case EN_COURS -> cible == MAITRISE;
            case MAITRISE -> cible == EN_COURS;   // le feu peut reprendre
            case CLOS -> false;
        };
    }
}
