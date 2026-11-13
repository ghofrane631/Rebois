package tn.reboistn.coordination.model;

/** Cycle de vie d'un incident : OUVERT -> EN_COURS (ou MOYENS_A_AFFECTER) -> MAITRISE -> CLOS. */
public enum StatutIncident {
    OUVERT, MOYENS_A_AFFECTER, EN_COURS, MAITRISE, CLOS
}
