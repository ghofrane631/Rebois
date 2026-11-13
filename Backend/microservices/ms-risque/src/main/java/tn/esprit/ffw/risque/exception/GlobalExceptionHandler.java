package tn.esprit.ffw.risque.exception;

import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<Map<String, Object>> reponse(HttpStatus s, String msg) {
        return ResponseEntity.status(s).body(Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", s.value(), "erreur", s.getReasonPhrase(), "message", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> validation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage()).collect(Collectors.joining("; "));
        return reponse(HttpStatus.BAD_REQUEST, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> argument(IllegalArgumentException ex) {
        return reponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(SecteurIntrouvableException.class)
    public ResponseEntity<Map<String, Object>> introuvable(SecteurIntrouvableException ex) {
        return reponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ServiceIndisponibleException.class)
    public ResponseEntity<Map<String, Object>> indisponible(ServiceIndisponibleException ex) {
        return reponse(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
    }
}
