package tn.esprit.ffw.risque.service;

import org.springframework.stereotype.Component;

/**
 * Formule de l'indice de risque d'incendie (0..100).
 *
 * scoreMeteo = 35% température + 25% (100 - humidité) + 20% vent + 20% jours sans pluie
 *   température : 10 °C -> 0   ... 45 °C -> 100
 *   vent        : 0 km/h -> 0  ... 60 km/h -> 100
 *   jours sans pluie : 0 j -> 0 ... 30 j -> 100
 *
 * indice = clamp(scoreMeteo + 5 x feuxActifs30j + ajustement, 0, 100)
 */
@Component
public class CalculIndiceService {

    public static final double POIDS_TEMPERATURE = 0.35;
    public static final double POIDS_HUMIDITE = 0.25;
    public static final double POIDS_VENT = 0.20;
    public static final double POIDS_SECHERESSE = 0.20;
    public static final int BONUS_PAR_FEU = 5;
    public static final int FENETRE_FEUX_JOURS = 30;
    public static final int AJUSTEMENT_RESTAURATION = -10;
    public static final int AJUSTEMENT_MIN = -30;

    public int scoreMeteo(double temperature, double humidite, double vent, int joursSansPluie) {
        double t = normaliser(temperature, 10, 45);
        double h = 100 - clamp(humidite, 0, 100);
        double v = normaliser(vent, 0, 60);
        double s = normaliser(joursSansPluie, 0, 30);
        double score = POIDS_TEMPERATURE * t + POIDS_HUMIDITE * h + POIDS_VENT * v + POIDS_SECHERESSE * s;
        return (int) Math.round(clamp(score, 0, 100));
    }

    public int indice(int scoreMeteo, int feuxActifs, int ajustement) {
        return (int) clamp(scoreMeteo + (long) BONUS_PAR_FEU * feuxActifs + ajustement, 0, 100);
    }

    private double normaliser(double valeur, double min, double max) {
        return clamp((valeur - min) / (max - min), 0, 1) * 100;
    }

    private double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
