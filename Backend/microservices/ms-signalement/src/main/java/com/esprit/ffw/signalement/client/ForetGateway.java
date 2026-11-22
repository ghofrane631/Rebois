package com.esprit.ffw.signalement.client;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Appel protégé vers ms-foret : retry sur erreur réseau, circuit breaker et repli.
 * Le repli renvoie Optional.empty() : le signalement est conservé en SECTEUR_A_VERIFIER.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ForetGateway {

    private final ForetClient client;

    @CircuitBreaker(name = "foret", fallbackMethod = "repli")
    @Retry(name = "foret")
    public Optional<SecteurDto> localiser(double lat, double lng) {
        return Optional.ofNullable(client.localiser(lat, lng));
    }

    private Optional<SecteurDto> repli(double lat, double lng, Exception e) {
        log.warn("Forêt indisponible pour ({}, {}) : {}", lat, lng, e.toString());
        return Optional.empty();
    }
}
