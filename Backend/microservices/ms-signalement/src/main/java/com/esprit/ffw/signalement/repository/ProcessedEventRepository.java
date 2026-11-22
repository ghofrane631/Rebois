package com.esprit.ffw.signalement.repository;

import com.esprit.ffw.signalement.event.ProcessedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProcessedEventRepository extends MongoRepository<ProcessedEvent, String> {
}
