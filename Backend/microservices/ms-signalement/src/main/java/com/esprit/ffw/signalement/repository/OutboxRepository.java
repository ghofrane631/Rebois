package com.esprit.ffw.signalement.repository;

import com.esprit.ffw.signalement.event.OutboxMessage;
import com.esprit.ffw.signalement.event.OutboxStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface OutboxRepository extends MongoRepository<OutboxMessage, String> {
    List<OutboxMessage> findTop50ByStatutAndNextAttemptAtLessThanEqualOrderByCreeLeAsc(
            OutboxStatus statut, Instant maintenant);

    long deleteByStatutAndEnvoyeLeBefore(OutboxStatus statut, Instant avant);
}
