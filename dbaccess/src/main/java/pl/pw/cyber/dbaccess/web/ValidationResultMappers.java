package pl.pw.cyber.dbaccess.web;

import org.springframework.http.ResponseEntity;
import pl.pw.cyber.dbaccess.web.validation.ValidationResult;

import java.util.function.Function;

public final class ValidationResultMappers {

    private ValidationResultMappers() {}

    public static final Function<ValidationResult, ResponseEntity<?>> TO_RESPONSE_ENTITY_MAPPER =
      result -> switch (result) {
          case ValidationResult.Valid __ -> ResponseEntity.ok().build();
          case ValidationResult.Invalid invalid -> ResponseEntity.badRequest().body(invalid.toProblemDetail());
      };
}
