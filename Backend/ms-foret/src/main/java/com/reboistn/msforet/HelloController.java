package com.reboistn.msforet;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Forêt")
public class HelloController {

    @GetMapping("/hello")
    @Operation(summary = "Vérifier que le service répond")
    public ResponseEntity<Map<String, String>> hello() {
        return ResponseEntity.ok(Map.of("service", "ms-foret", "status", "UP"));
    }
}
