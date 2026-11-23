package com.reboistn.foretservice.config;

import com.reboistn.foretservice.entity.Delegation;
import com.reboistn.foretservice.entity.Region;
import com.reboistn.foretservice.repository.DelegationRepository;
import com.reboistn.foretservice.repository.RegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final RegionRepository regionRepository;
    private final DelegationRepository delegationRepository;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            if (regionRepository.count() > 0) {
                log.info("Données déjà présentes, initialisation ignorée.");
                return;
            }

            log.info("Initialisation des données de référence (régions/délégations)...");

            // Régions tunisiennes avec délégations forestières
            Map<String[], List<String>> regionsData = Map.of(
                    new String[]{"Région du Nord-Ouest", "Jendouba"},
                    List.of("Jendouba Nord", "Jendouba Sud", "Bou Salem", "Tabarka", "Aïn Draham", "Fernana"),

                    new String[]{"Région du Nord", "Bizerte"},
                    List.of("Bizerte Nord", "Bizerte Sud", "Mateur", "Menzel Bourguiba", "Sejnane", "Ghezala"),

                    new String[]{"Région du Centre-Ouest", "Kasserine"},
                    List.of("Kasserine Nord", "Kasserine Sud", "Sbeitla", "Thala", "Fériana", "Foussana"),

                    new String[]{"Région du Nord-Est", "Béja"},
                    List.of("Béja Nord", "Béja Sud", "Medjez el-Bab", "Nefza", "Testour"),

                    new String[]{"Région du Centre", "Le Kef"},
                    List.of("Le Kef Ouest", "Le Kef Est", "Sakiet Sidi Youssef", "Nebeur", "Tajerouine")
            );

            regionsData.forEach((regionData, delegNames) -> {
                Region region = regionRepository.save(
                        Region.builder()
                                .nom(regionData[0])
                                .gouvernorat(regionData[1])
                                .build()
                );
                delegNames.forEach(delegNom ->
                        delegationRepository.save(
                                Delegation.builder()
                                        .nom(delegNom)
                                        .region(region)
                                        .build()
                        )
                );
                log.info("Région '{}' créée avec {} délégations.", region.getNom(), delegNames.size());
            });

            log.info("Initialisation terminée : {} régions insérées.", regionRepository.count());
        };
    }
}
