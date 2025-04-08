package pl.pw.cyber.dbaccess.infrastructure.spring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@Slf4j
@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public void handleSecurityException(SecurityException ex, HttpServletResponse response) throws IOException {
        log.warn("Security exception: {}", ex.getMessage());
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public void handleBadRequest(Exception ex, HttpServletResponse response) throws IOException {
        log.warn("Bad request: {}", ex.getMessage());
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Bad request");
    }

    @ExceptionHandler(Throwable.class)
    public void handleAnything(Throwable t, HttpServletRequest req, HttpServletResponse res) throws IOException {
        log.error("💥 Unhandled throwable at {}: {}", req.getRequestURI(), t.toString());
        res.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected server error");
    }
}
