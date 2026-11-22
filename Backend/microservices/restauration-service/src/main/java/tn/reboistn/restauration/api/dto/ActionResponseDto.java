package tn.reboistn.restauration.api.dto;

import tn.reboistn.restauration.domain.ActionRestauration;
import tn.reboistn.restauration.domain.TypeAction;

public record ActionResponseDto(Long id, TypeAction type, String description, boolean realisee) {
    public static ActionResponseDto from(ActionRestauration action) {
        return new ActionResponseDto(action.getId(), action.getType(), action.getDescription(), action.isRealisee());
    }
}