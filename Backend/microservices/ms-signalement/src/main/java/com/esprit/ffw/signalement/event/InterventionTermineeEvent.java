package com.esprit.ffw.signalement.event;

import java.util.UUID;

/** Publié par Coordination de crise. signalementId est à convenir avec Mezen (voir README). */
public record InterventionTermineeEvent(UUID eventId, Long incidentId, String signalementId,
                                        Long secteurId, Double surfaceBruleeHa) {}
