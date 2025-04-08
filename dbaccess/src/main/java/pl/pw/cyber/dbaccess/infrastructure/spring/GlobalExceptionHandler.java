package pl.pw.cyber.dbaccess.infrastructure.spring;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

@Slf4j
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.badRequest().body(Map.of("error", "Bad request"));
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<Map<String, String>> handleGeneric(HttpServletRequest req, Throwable ex) {
        log.error("💥 Unhandled throwable at {}: {}", req.getRequestURI(), ex.toString());
        return ResponseEntity.internalServerError().body(Map.of("error", "Unexpected server error"));
    }
}
