package pl.pw.cyber.dbaccess.web.validation;


import org.springframework.http.ResponseEntity;

public final class ValidationResponseBuilder {

    private ValidationResponseBuilder() {}

    public static ResponseEntity<?> fromResult(ValidationResult result) {
        return switch (result) {
            case ValidationResult.Valid __ -> ResponseEntity.ok().build();
            case ValidationResult.Invalid invalid -> ResponseEntity.badRequest().body(invalid.toProblemDetail());
        };
    }
}
