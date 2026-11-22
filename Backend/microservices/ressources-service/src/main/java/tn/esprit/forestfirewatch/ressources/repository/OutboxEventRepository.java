package tn.esprit.forestfirewatch.ressources.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.forestfirewatch.ressources.entity.OutboxEvent;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, String> {

    List<OutboxEvent> findTop50ByPublieFalseOrderByCreeLeAsc();

    List<OutboxEvent> findByType(String type);
}
