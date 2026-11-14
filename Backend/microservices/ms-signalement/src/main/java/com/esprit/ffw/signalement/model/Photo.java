package com.esprit.ffw.signalement.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor
public class Photo {
    private String url;
    private LocalDateTime priseLe;
}
