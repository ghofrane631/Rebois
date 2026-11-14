package com.esprit.ffw.signalement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class HistoriqueStatut {
    private Statut ancien;
    private Statut nouveau;
    private String gardeId;
    private String motif;
    private LocalDateTime date;
}
