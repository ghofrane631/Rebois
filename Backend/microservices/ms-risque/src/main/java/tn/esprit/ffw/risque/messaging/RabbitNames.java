package tn.esprit.ffw.risque.messaging;

public final class RabbitNames {
    private RabbitNames() {}
    public static final String EXCHANGE = "ffw.events";

    // routing keys
    public static final String RK_SECTEUR_CREE = "secteur.cree";
    public static final String RK_FEU_CONFIRME = "feu.confirme";
    public static final String RK_INTERVENTION_TERMINEE = "intervention.terminee";
    public static final String RK_ZONE_RESTAUREE = "zone.restauree";
    public static final String RK_NIVEAU_CHANGE = "risque.niveau.change";

    // files de ms-risque
    public static final String Q_SECTEUR_CREE = "risque.secteur-cree";
    public static final String Q_FEU_CONFIRME = "risque.feu-confirme";
    public static final String Q_INTERVENTION_TERMINEE = "risque.intervention-terminee";
    public static final String Q_ZONE_RESTAUREE = "risque.zone-restauree";
    public static final String DLX = "ffw.events.dlx";
}
