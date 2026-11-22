package tn.esprit.ffw.risque.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SecteurDto(Long id, String nom) { }
