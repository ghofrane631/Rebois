package com.esprit.ffw.signalement.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> body(HttpStatus s, String code, String msg) {
        return ResponseEntity.status(s).body(Map.of("status", s.value(), "code", code, "message", msg));
    }

    @ExceptionHandler(SignalementIntrouvableException.class)
    ResponseEntity<Map<String, Object>> notFound(SignalementIntrouvableException e) {
        return body(HttpStatus.NOT_FOUND, "SIGNALEMENT_INTROUVABLE", e.getMessage());
    }

    @ExceptionHandler(TransitionInvalideException.class)
    ResponseEntity<Map<String, Object>> conflict(TransitionInvalideException e) {
        return body(HttpStatus.CONFLICT, "TRANSITION_INVALIDE", e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .reduce((a, b) -> a + " ; " + b).orElse("Requête invalide");
        return body(HttpStatus.BAD_REQUEST, "DONNEES_INVALIDES", msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, Object>> badArg(IllegalArgumentException e) {
        return body(HttpStatus.BAD_REQUEST, "PARAMETRE_INVALIDE", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> forbidden(AccessDeniedException e) {
        return body(HttpStatus.FORBIDDEN, "ACCES_REFUSE", e.getMessage());
    }
}
