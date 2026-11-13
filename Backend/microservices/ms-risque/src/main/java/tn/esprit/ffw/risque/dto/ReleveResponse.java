package tn.esprit.ffw.risque.dto;

import tn.esprit.ffw.risque.entity.NiveauRisque;
import java.time.LocalDateTime;

public record ReleveResponse(Long id, Long secteurId, LocalDateTime dateReleve, double temperature,
                             double humidite, double vent, int joursSansPluie,
                             int indice, NiveauRisque niveau) { }
