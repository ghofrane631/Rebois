package com.esprit.ffw.signalement.dto;

import com.esprit.ffw.signalement.model.Signalement;
import java.util.List;

/** Le signalement créé + les ids des signalements proches (doublons possibles). */
public record SignalementResponse(Signalement signalement, List<String> doublonsProches) {}
