package tn.esprit.ffw.risque.messaging;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tn.esprit.ffw.risque.events.NiveauRisqueChange;

/** Publication ASYNCHRONE de NiveauRisqueChange, uniquement si la transaction a été validée. */
@Component
@RequiredArgsConstructor
public class NiveauRisquePublisher {
    private static final Logger log = LoggerFactory.getLogger(NiveauRisquePublisher.class);
    private final RabbitTemplate rabbit;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publier(NiveauRisqueChange e) {
        try {
            rabbit.convertAndSend(RabbitNames.EXCHANGE, RabbitNames.RK_NIVEAU_CHANGE, e);
            log.info("NiveauRisqueChange publié : secteur {} {} -> {}", e.secteurId(), e.ancienNiveau(), e.nouveauNiveau());
        } catch (Exception ex) {
            // Le changement est déjà en base ; on journalise sans casser la réponse HTTP.
            log.error("Échec de publication NiveauRisqueChange (secteur {}) : {}", e.secteurId(), ex.getMessage());
        }
    }
}
