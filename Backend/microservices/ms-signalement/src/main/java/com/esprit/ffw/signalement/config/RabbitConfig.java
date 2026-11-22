package com.esprit.ffw.signalement.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper.TypePrecedence;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "ffw.events";
    public static final String DLX = "ffw.events.dlx";
    public static final String QUEUE_INTERVENTION = "signalement.intervention-terminee";
    public static final String QUEUE_INTERVENTION_DLQ = "signalement.intervention-terminee.dlq";
    public static final String KEY_FEU_CONFIRME = "feu.confirme";
    public static final String KEY_INTERVENTION = "intervention.terminee";
    public static final String KEY_INTERVENTION_DLQ = "intervention.terminee.dlq";

    @Bean
    Declarables signalementDeclarables() {
        TopicExchange exchange = new TopicExchange(EXCHANGE, true, false);
        TopicExchange deadLetterExchange = new TopicExchange(DLX, true, false);

        Queue interventionQueue = QueueBuilder.durable(QUEUE_INTERVENTION)
                .deadLetterExchange(DLX)
                .deadLetterRoutingKey(KEY_INTERVENTION_DLQ)
                .build();
        Queue interventionDlq = QueueBuilder.durable(QUEUE_INTERVENTION_DLQ).build();

        Binding interventionBinding = BindingBuilder.bind(interventionQueue).to(exchange).with(KEY_INTERVENTION);
        Binding dlqBinding = BindingBuilder.bind(interventionDlq).to(deadLetterExchange).with(KEY_INTERVENTION_DLQ);

        return new Declarables(exchange, deadLetterExchange, interventionQueue, interventionDlq,
                interventionBinding, dlqBinding);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        Jackson2JsonMessageConverter c = new Jackson2JsonMessageConverter();
        // Désérialisation selon le type du paramètre du listener, pas selon la classe de l'émetteur
        c.setTypePrecedence(TypePrecedence.INFERRED);
        return c;
    }
}
