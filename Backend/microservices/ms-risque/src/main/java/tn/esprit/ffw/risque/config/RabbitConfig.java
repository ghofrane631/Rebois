package tn.esprit.ffw.risque.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tn.esprit.ffw.risque.messaging.RabbitNames;

@Configuration
public class RabbitConfig {

    @Bean TopicExchange ffwExchange() { return new TopicExchange(RabbitNames.EXCHANGE, true, false); }
    @Bean FanoutExchange dlx() { return new FanoutExchange(RabbitNames.DLX, true, false); }
    @Bean Queue dlq() { return QueueBuilder.durable("risque.dlq").build(); }
    @Bean Binding dlqBinding() { return BindingBuilder.bind(dlq()).to(dlx()); }

    private Queue file(String nom) {
        return QueueBuilder.durable(nom).withArgument("x-dead-letter-exchange", RabbitNames.DLX).build();
    }

    @Bean Queue qSecteurCree() { return file(RabbitNames.Q_SECTEUR_CREE); }
    @Bean Queue qFeuConfirme() { return file(RabbitNames.Q_FEU_CONFIRME); }
    @Bean Queue qInterventionTerminee() { return file(RabbitNames.Q_INTERVENTION_TERMINEE); }
    @Bean Queue qZoneRestauree() { return file(RabbitNames.Q_ZONE_RESTAUREE); }

    @Bean Binding b1() { return BindingBuilder.bind(qSecteurCree()).to(ffwExchange()).with(RabbitNames.RK_SECTEUR_CREE); }
    @Bean Binding b2() { return BindingBuilder.bind(qFeuConfirme()).to(ffwExchange()).with(RabbitNames.RK_FEU_CONFIRME); }
    @Bean Binding b3() { return BindingBuilder.bind(qInterventionTerminee()).to(ffwExchange()).with(RabbitNames.RK_INTERVENTION_TERMINEE); }
    @Bean Binding b4() { return BindingBuilder.bind(qZoneRestauree()).to(ffwExchange()).with(RabbitNames.RK_ZONE_RESTAUREE); }

    @Bean MessageConverter jsonConverter() { return new Jackson2JsonMessageConverter(); }
}
