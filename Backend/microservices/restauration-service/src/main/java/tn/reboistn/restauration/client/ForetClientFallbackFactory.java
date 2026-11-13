package tn.reboistn.restauration.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Component
public class ForetClientFallbackFactory implements FallbackFactory<ForetClient> {

    @Override
    public ForetClient create(Throwable cause) {
        return secteurId -> null;
    }
}