package tn.esprit.ffw.risque.dto;

import tn.esprit.ffw.risque.entity.NiveauRisque;
import java.time.LocalDateTime;

public record IndiceResponse(Long secteurId, int indice, NiveauRisque niveau, int feuxRecents, LocalDateTime majLe) { }
