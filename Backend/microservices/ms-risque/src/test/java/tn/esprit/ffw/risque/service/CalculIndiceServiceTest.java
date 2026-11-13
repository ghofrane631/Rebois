package tn.esprit.ffw.risque.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculIndiceServiceTest {

    private final CalculIndiceService calcul = new CalculIndiceService();

    @Test
    void conditionsCalmesDonnentUnScoreFaible() {
        assertTrue(calcul.scoreMeteo(12, 85, 5, 0) < 25);
    }

    @Test
    void scenarioDemoDonneUnNiveauExtreme() {
        // 41 °C, 12 % d'humidité, 50 km/h, 30 jours sans pluie
        int score = calcul.scoreMeteo(41, 12, 50, 30);
        assertTrue(score >= 85, "score = " + score);
    }

    @Test
    void indiceEstPlafonneA100() {
        assertEquals(100, calcul.indice(98, 4, 0));
    }

    @Test
    void indiceNeDescendPasSousZero() {
        assertEquals(0, calcul.indice(5, 0, -30));
    }

    @Test
    void chaqueFeuActifAjouteUnBonus() {
        assertEquals(60, calcul.indice(50, 2, 0));
    }
}
