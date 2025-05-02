package pl.pw.cyber.dbaccess.infrastructure.spring;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import pl.pw.cyber.dbaccess.common.result.ResultExecutionException;

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
        log.error("💥 Unhandled throwable at {}: {}", req.getRequestURI(), ex.getMessage());
        return ResponseEntity.internalServerError().body(Map.of("error", "Unexpected server error"));
    }

    @ExceptionHandler(ResultExecutionException.class)
    public ResponseEntity<ProblemDetail> handleResultExecution(ResultExecutionException ex, HttpServletRequest req) {
        log.error("Result execution failed at {}: {}", req.getRequestURI(), ex.getCause().toString());

        var pd = ProblemDetail.forStatus(500);
        pd.setTitle("Internal Server Error");
        pd.setDetail("Unexpected processing error");

        return ResponseEntity.internalServerError().body(pd);
    }
}
