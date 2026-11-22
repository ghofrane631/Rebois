package tn.esprit.forestfirewatch.ressources.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Topologie RabbitMQ du service Ressources.
 *
 *  forestfire.events (topic)  --NiveauRisqueChange-->   ressources.niveau-risque-change
 *                             --InterventionTerminee--> ressources.intervention-terminee
 *  Après 3 essais ratés, le message part dans ressources.dlq (dead-letter queue).
 */
@Configuration
public class RabbitConfig {

    public static final String EXCHANGE = "forestfire.events";
    public static final String DLX = "forestfire.dlx";

    public static final String Q_NIVEAU_RISQUE = "ressources.niveau-risque-change";
    public static final String Q_INTERVENTION_TERMINEE = "ressources.intervention-terminee";
    public static final String DLQ = "ressources.dlq";

    @Bean
    TopicExchange evenementsExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(DLX, true, false);
    }

    @Bean
    Queue niveauRisqueQueue() {
        return QueueBuilder.durable(Q_NIVEAU_RISQUE)
                .deadLetterExchange(DLX).deadLetterRoutingKey(DLQ).build();
    }

    @Bean
    Queue interventionTermineeQueue() {
        return QueueBuilder.durable(Q_INTERVENTION_TERMINEE)
                .deadLetterExchange(DLX).deadLetterRoutingKey(DLQ).build();
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Binding bindNiveauRisque() {
        return BindingBuilder.bind(niveauRisqueQueue()).to(evenementsExchange()).with("NiveauRisqueChange");
    }

    @Bean
    Binding bindInterventionTerminee() {
        return BindingBuilder.bind(interventionTermineeQueue()).to(evenementsExchange()).with("InterventionTerminee");
    }

    @Bean
    Binding bindDlq() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }

    /** JSON <-> record Java, d'après le type du paramètre du listener (pas d'en-tête de classe Java). */
    @Bean
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }
}
