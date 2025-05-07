package pl.pw.cyber.dbaccess.infrastructure.spring;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import pl.pw.cyber.dbaccess.common.result.ResultExecutionException;

import java.util.Optional;

@Slf4j
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Bad request: {}", ex.getMessage());

        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Bad Request");
        pd.setDetail("Invalid input or argument");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(HttpServletRequest req, NoResourceFoundException ex) {
        log.warn("🔍 Resource not found at {}: {}", req.getRequestURI(), ex.getMessage());

        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problem.setTitle("Not Found");
        problem.setDetail(ex.getMessage());
        problem.setProperty("path", req.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problem);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ProblemDetail> handleGeneric(HttpServletRequest req, Throwable ex) {
        log.error("💥 Unhandled throwable at {}: {}", req.getRequestURI(), ex.getMessage());
        var pd = ProblemDetail.forStatus(500);
        pd.setTitle("Internal Server Error");
        pd.setDetail("Unexpected processing error");

        return ResponseEntity.internalServerError().body(pd);
    }

    @ExceptionHandler(ResultExecutionException.class)
    public ResponseEntity<ProblemDetail> handleResultExecution(ResultExecutionException ex, HttpServletRequest req) {
        log.error("Result execution failed at {}: {}", req.getRequestURI(),
          Optional.ofNullable(ex.getCause()).map(Throwable::toString).orElse("no cause")
        );
        var pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        pd.setDetail("Unexpected processing error");

        return ResponseEntity.internalServerError().body(pd);
    }
}
