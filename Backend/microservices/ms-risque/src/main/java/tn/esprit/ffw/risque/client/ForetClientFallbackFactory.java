package tn.esprit.ffw.risque.client;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;
import tn.esprit.ffw.risque.dto.SecteurDto;
import tn.esprit.ffw.risque.exception.SecteurIntrouvableException;
import tn.esprit.ffw.risque.exception.ServiceIndisponibleException;

/**
 * 404 de Forêt  -> secteur inconnu (HTTP 404 chez nous).
 * Tout le reste -> Forêt indisponible (HTTP 503) : on refuse le relevé car il doit viser un secteur connu.
 */
@Component
public class ForetClientFallbackFactory implements FallbackFactory<ForetClient> {
    private static final Logger log = LoggerFactory.getLogger(ForetClientFallbackFactory.class);

    @Override
    public ForetClient create(Throwable cause) {
        return id -> {
            if (cause instanceof FeignException.NotFound) {
                throw new SecteurIntrouvableException(id);
            }
            log.warn("Service Forêt indisponible (secteur {}) : {}", id, cause.toString());
            throw new ServiceIndisponibleException("Service Forêt indisponible, relevé refusé");
        };
    }
}
