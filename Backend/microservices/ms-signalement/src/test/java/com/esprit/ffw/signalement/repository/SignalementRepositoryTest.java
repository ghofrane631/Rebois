package com.esprit.ffw.signalement.repository;

import com.esprit.ffw.signalement.model.Signalement;
import com.esprit.ffw.signalement.model.Statut;
import com.esprit.ffw.signalement.model.TypeSignalement;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest
@Testcontainers(disabledWithoutDocker = true)
class SignalementRepositoryTest {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:7.0");

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", MONGO::getReplicaSetUrl);
        registry.add("spring.data.mongodb.auto-index-creation", () -> "true");
    }

    @Autowired SignalementRepository repo;

    private Signalement signalement(String id, double lng, double lat, Statut statut, Instant creeLe) {
        Signalement s = new Signalement();
        s.setId(id);
        s.setAuteurId("u-1");
        s.setType(TypeSignalement.FUMEE);
        s.setPosition(new GeoJsonPoint(lng, lat));
        s.setIntensite(3);
        s.setStatut(statut);
        s.setCreeLe(creeLe);
        return s;
    }

    @Test
    void laRequeteNearFiltreParRayonEtParDate() {
        Instant maintenant = Instant.now();
        repo.save(signalement("near", 10.0800, 36.4200, Statut.SIGNALE, maintenant));
        repo.save(signalement("closed", 10.0802, 36.4202, Statut.CLOS, maintenant));
        repo.save(signalement("far", 10.9000, 36.9000, Statut.SIGNALE, maintenant));
        repo.save(signalement("old", 10.0801, 36.4201, Statut.SIGNALE,
                maintenant.minus(60, ChronoUnit.MINUTES)));

        List<Signalement> resultat = repo.findByPositionNearAndCreeLeAfter(
                new GeoJsonPoint(10.08, 36.42),
                new Distance(500, Metrics.NEUTRAL),
                maintenant.minus(30, ChronoUnit.MINUTES));

        Set<String> ids = resultat.stream().map(Signalement::getId).collect(Collectors.toSet());
        assertEquals(Set.of("near", "closed"), ids);
    }

    @Test
    void laVersionEstIncrementeeALaMiseAJour() {
        Signalement saved = repo.save(signalement("v", 10.08, 36.42, Statut.SIGNALE, Instant.now()));
        Long initiale = saved.getVersion();
        assertNotNull(initiale);

        saved.setDescription("mise à jour");
        Signalement updated = repo.save(saved);

        assertTrue(updated.getVersion() > initiale);
    }

    @Test
    void rechercheParSecteurEtStatut() {
        Signalement a = signalement("a", 10.08, 36.42, Statut.CONFIRME, Instant.now());
        a.setSecteurId(14L);
        Signalement b = signalement("b", 10.09, 36.43, Statut.CONFIRME, Instant.now());
        b.setSecteurId(15L);
        repo.saveAll(List.of(a, b));

        List<Signalement> resultat = repo.findBySecteurIdAndStatut(14L, Statut.CONFIRME);

        assertEquals(1, resultat.size());
        assertEquals("a", resultat.get(0).getId());
    }
}
