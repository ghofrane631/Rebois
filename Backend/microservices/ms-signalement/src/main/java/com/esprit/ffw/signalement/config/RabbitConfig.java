package com.esprit.ffw.signalement.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper.TypePrecedence;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "ffw.events";
    public static final String QUEUE_INTERVENTION = "signalement.intervention-terminee";
    public static final String KEY_FEU_CONFIRME = "feu.confirme";
    public static final String KEY_INTERVENTION = "intervention.terminee";

    @Bean
    TopicExchange ffwExchange() { return new TopicExchange(EXCHANGE, true, false); }

    @Bean
    Queue interventionQueue() { return QueueBuilder.durable(QUEUE_INTERVENTION).build(); }

    @Bean
    Binding interventionBinding(Queue interventionQueue, TopicExchange ffwExchange) {
        return BindingBuilder.bind(interventionQueue).to(ffwExchange).with(KEY_INTERVENTION);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonConverter() {
        Jackson2JsonMessageConverter c = new Jackson2JsonMessageConverter();
        // Désérialisation selon le type du paramètre du listener, pas selon la classe de l'émetteur
        c.setTypePrecedence(TypePrecedence.INFERRED);
        return c;
    }
}
