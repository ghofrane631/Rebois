package com.reboistn.foretservice.mapper;

import com.reboistn.foretservice.dto.request.ZoneForestiereRequest;
import com.reboistn.foretservice.dto.response.ZoneForestiereResponse;
import com.reboistn.foretservice.entity.Delegation;
import com.reboistn.foretservice.entity.StatutZone;
import com.reboistn.foretservice.entity.ZoneForestiere;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.stereotype.Component;

@Component
public class ZoneForestiereMapper {

    public ZoneForestiere toEntity(ZoneForestiereRequest request, Delegation delegation) {
        Polygon geometrie = null;
        if (request.getGeometrieWkt() != null && !request.getGeometrieWkt().isBlank()) {
            geometrie = parsePolygon(request.getGeometrieWkt());
        }
        return ZoneForestiere.builder()
                .nom(request.getNom())
                .type(request.getType())
                .superficieHa(request.getSuperficieHa())
                .description(request.getDescription())
                .geometrie(geometrie)
                .statut(StatutZone.ACTIVE)
                .delegation(delegation)
                .build();
    }

    public void updateEntity(ZoneForestiere zone, ZoneForestiereRequest request, Delegation delegation) {
        zone.setNom(request.getNom());
        zone.setType(request.getType());
        zone.setSuperficieHa(request.getSuperficieHa());
        zone.setDescription(request.getDescription());
        zone.setDelegation(delegation);
        if (request.getGeometrieWkt() != null && !request.getGeometrieWkt().isBlank()) {
            zone.setGeometrie(parsePolygon(request.getGeometrieWkt()));
        } else {
            zone.setGeometrie(null);
        }
    }

    public ZoneForestiereResponse toResponse(ZoneForestiere zone) {
        String wkt = null;
        if (zone.getGeometrie() != null) {
            wkt = zone.getGeometrie().toText();
        }
        return ZoneForestiereResponse.builder()
                .id(zone.getId())
                .nom(zone.getNom())
                .type(zone.getType())
                .superficieHa(zone.getSuperficieHa())
                .description(zone.getDescription())
                .geometrieWkt(wkt)
                .statut(zone.getStatut())
                .dateCreation(zone.getDateCreation())
                .dateModification(zone.getDateModification())
                .delegationId(zone.getDelegation().getId())
                .delegationNom(zone.getDelegation().getNom())
                .regionId(zone.getDelegation().getRegion().getId())
                .regionNom(zone.getDelegation().getRegion().getNom())
                .build();
    }

    private Polygon parsePolygon(String wkt) {
        try {
            WKTReader reader = new WKTReader();
            Geometry geom = reader.read(wkt);
            if (!(geom instanceof Polygon polygon)) {
                throw new IllegalArgumentException("La géométrie fournie n'est pas un Polygon WKT valide");
            }
            polygon.setSRID(4326);
            return polygon;
        } catch (ParseException e) {
            throw new IllegalArgumentException("WKT Polygon invalide : " + e.getMessage());
        }
    }
}
