package tn.reboistn.restauration.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tn.reboistn.restauration.api.dto.ActionResponseDto;
import tn.reboistn.restauration.api.dto.ActionStatusRequest;
import tn.reboistn.restauration.api.dto.EvaluationRequest;
import tn.reboistn.restauration.api.dto.PlanRequest;
import tn.reboistn.restauration.api.dto.StatusUpdateRequest;
import tn.reboistn.restauration.api.dto.SuiviRequest;
import tn.reboistn.restauration.api.dto.ZoneResponse;
import tn.reboistn.restauration.domain.StatutZone;
import tn.reboistn.restauration.service.RestaurationService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/restauration")
public class RestaurationController {

    private final RestaurationService restaurationService;

    public RestaurationController(RestaurationService restaurationService) {
        this.restaurationService = restaurationService;
    }

    @GetMapping("/zones")
    public List<ZoneResponse> getZones(@RequestParam(required = false) StatutZone statut,
                                       @RequestParam(required = false) String gouvernorat) {
        return restaurationService.findZones(statut, gouvernorat);
    }

    @GetMapping("/zones/{id}")
    public ZoneResponse getZone(@PathVariable Long id) {
        return restaurationService.getZone(id);
    }

    @PatchMapping("/zones/{id}/statut")
    public ZoneResponse updateStatus(@PathVariable Long id, @Valid @RequestBody StatusUpdateRequest request) {
        return restaurationService.updateStatus(id, request.statut());
    }

    @PostMapping("/zones/{id}/evaluation")
    public ResponseEntity<ZoneResponse> evaluate(@PathVariable Long id,
                                                 @Valid @RequestBody EvaluationRequest request) {
        ZoneResponse response = restaurationService.evaluate(id, request);
        return ResponseEntity.created(zoneUri(id)).body(response);
    }

    @PostMapping("/zones/{id}/plan")
    public ResponseEntity<ZoneResponse> createPlan(@PathVariable Long id,
                                                   @Valid @RequestBody PlanRequest request) {
        ZoneResponse response = restaurationService.createPlan(id, request);
        return ResponseEntity.created(zoneUri(id)).body(response);
    }

    @PatchMapping("/actions/{id}")
    public ActionResponseDto updateAction(@PathVariable Long id,
                                          @Valid @RequestBody ActionStatusRequest request) {
        return ActionResponseDto.from(restaurationService.updateAction(id, request.realisee()));
    }

    @PostMapping("/zones/{id}/suivis")
    public ResponseEntity<ZoneResponse> addSuivi(@PathVariable Long id,
                                                 @Valid @RequestBody SuiviRequest request) {
        ZoneResponse response = restaurationService.addSuivi(id, request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().build().toUri();
        return ResponseEntity.status(HttpStatus.CREATED).location(location).body(response);
    }

    private URI zoneUri(Long id) {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/restauration/zones/{id}").buildAndExpand(id).toUri();
    }
}