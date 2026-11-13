package tn.reboistn.coordination.messaging;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Une file par événement consommé, liée à l'exchange commun.
 * Un message en échec après les essais part dans la file d'erreurs (dead-letter).
 */
@Configuration
public class RabbitConfig {

    public static final String Q_FEU_CONFIRME = "coordination.feu-confirme";
    public static final String Q_NIVEAU_RISQUE = "coordination.niveau-risque-change";
    public static final String Q_MOYENS_INSUFFISANTS = "coordination.moyens-insuffisants";
    public static final String Q_UTILISATEUR_CREE = "coordination.utilisateur-cree";
    public static final String DLQ = "coordination.dlq";

    @Value("${evenements.exchange}")
    private String exchange;

    @Value("${evenements.dead-letter-exchange}")
    private String deadLetterExchange;

    @Bean
    TopicExchange evenementsExchange() {
        return new TopicExchange(exchange, true, false);
    }

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange, true, false);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(DLQ).build();
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(DLQ);
    }

    @Bean
    Declarables filesConsommees(@Value("${evenements.cles.feu-confirme}") String feuConfirme,
                                @Value("${evenements.cles.niveau-risque-change}") String niveauRisque,
                                @Value("${evenements.cles.moyens-insuffisants}") String moyensInsuffisants,
                                @Value("${evenements.cles.utilisateur-cree}") String utilisateurCree) {
        return new Declarables(
                file(Q_FEU_CONFIRME), lien(Q_FEU_CONFIRME, feuConfirme),
                file(Q_NIVEAU_RISQUE), lien(Q_NIVEAU_RISQUE, niveauRisque),
                file(Q_MOYENS_INSUFFISANTS), lien(Q_MOYENS_INSUFFISANTS, moyensInsuffisants),
                file(Q_UTILISATEUR_CREE), lien(Q_UTILISATEUR_CREE, utilisateurCree));
    }

    /** Les événements circulent en JSON. */
    @Bean
    MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    private Queue file(String nom) {
        return QueueBuilder.durable(nom)
                .deadLetterExchange(deadLetterExchange)
                .deadLetterRoutingKey(DLQ)
                .build();
    }

    private Binding lien(String file, String cle) {
        return new Binding(file, Binding.DestinationType.QUEUE, exchange, cle, null);
    }
}
