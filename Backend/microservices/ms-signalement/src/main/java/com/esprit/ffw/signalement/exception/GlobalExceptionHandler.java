package com.esprit.ffw.signalement.exception;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> body(HttpStatus s, String code, String msg) {
        return ResponseEntity.status(s).body(Map.of(
                "status", s.value(),
                "code", code,
                "message", msg == null ? s.getReasonPhrase() : msg));
    }

    @ExceptionHandler(SignalementIntrouvableException.class)
    ResponseEntity<Map<String, Object>> notFound(SignalementIntrouvableException e) {
        return body(HttpStatus.NOT_FOUND, "SIGNALEMENT_INTROUVABLE", e.getMessage());
    }

    @ExceptionHandler(TransitionInvalideException.class)
    ResponseEntity<Map<String, Object>> conflict(TransitionInvalideException e) {
        return body(HttpStatus.CONFLICT, "TRANSITION_INVALIDE", e.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<Map<String, Object>> concurrentModification(OptimisticLockingFailureException e) {
        log.warn("Conflit de concurrence : {}", e.getMessage());
        return body(HttpStatus.CONFLICT, "CONFLIT_CONCURRENCE",
                "Le signalement a été modifié entre-temps, merci de réessayer");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .reduce((a, b) -> a + " ; " + b).orElse("Requête invalide");
        return body(HttpStatus.BAD_REQUEST, "DONNEES_INVALIDES", msg);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<Map<String, Object>> constraint(ConstraintViolationException e) {
        return body(HttpStatus.BAD_REQUEST, "DONNEES_INVALIDES", e.getMessage());
    }

    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<Map<String, Object>> badArg(Exception e) {
        return body(HttpStatus.BAD_REQUEST, "PARAMETRE_INVALIDE", e.getMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> unreadable(HttpMessageNotReadableException e) {
        log.debug("Corps de requête illisible", e);
        return body(HttpStatus.BAD_REQUEST, "CORPS_INVALIDE",
                "JSON invalide ou valeur d'énumération inconnue");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<Map<String, Object>> methodNotAllowed(HttpRequestMethodNotSupportedException e) {
        return body(HttpStatus.METHOD_NOT_ALLOWED, "METHODE_NON_AUTORISEE", e.getMessage());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<Map<String, Object>> mediaType(HttpMediaTypeNotSupportedException e) {
        return body(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "TYPE_MEDIA_NON_SUPPORTE", e.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<Map<String, Object>> noResource(NoResourceFoundException e) {
        return body(HttpStatus.NOT_FOUND, "RESSOURCE_INTROUVABLE", e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<Map<String, Object>> forbidden(AccessDeniedException e) {
        return body(HttpStatus.FORBIDDEN, "ACCES_REFUSE", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> unexpected(Exception e) {
        log.error("Erreur non gérée", e);
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "ERREUR_INTERNE",
                "Erreur interne, contactez l'administrateur");
    }
}
