package tn.esprit.ffw.risque.dto;

import tn.esprit.ffw.risque.entity.NiveauRisque;
import java.time.LocalDateTime;

public record HistoriqueResponse(Long secteurId, NiveauRisque ancien, NiveauRisque nouveau,
                                 int indice, String cause, LocalDateTime date) { }
