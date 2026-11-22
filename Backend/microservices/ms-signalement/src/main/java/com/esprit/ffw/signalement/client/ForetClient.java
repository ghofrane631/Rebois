package com.esprit.ffw.signalement.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Nom Eureka du service Forêt (ms-foret) : spring.application.name = foret */
@FeignClient(name = "FORET")
public interface ForetClient {
    @GetMapping("/api/secteurs/localiser")
    SecteurDto localiser(@RequestParam("lat") double lat, @RequestParam("lng") double lng);
}
