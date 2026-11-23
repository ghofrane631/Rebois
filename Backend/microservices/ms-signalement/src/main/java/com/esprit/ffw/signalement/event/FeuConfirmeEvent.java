package com.esprit.ffw.signalement.event;

import java.time.Instant;
import java.util.UUID;

public record FeuConfirmeEvent(UUID eventId, String signalementId, Long secteurId,
                               double latitude, double longitude, int intensite, Instant occurredAt) {}
