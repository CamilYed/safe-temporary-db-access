package pl.pw.cyber.dbaccess.web.validation;

import br.com.fluentvalidator.context.Error;
import org.springframework.http.ProblemDetail;

import java.util.List;
import java.util.stream.Collectors;

public sealed interface ValidationResult {

   ValidationResult VALID = new Valid();

    record Valid() implements ValidationResult {
    }

    record Invalid(List<String> errors) implements ValidationResult {

        public ProblemDetail toProblemDetail() {
            return ProblemDetailBuilder.validationError(errors);
        }
    }

    static ValidationResult from(br.com.fluentvalidator.context.ValidationResult fluentValidationResult) {
        if (fluentValidationResult.isValid()) {
            return VALID;
        } else {
            return new Invalid(
              fluentValidationResult.getErrors().stream()
                .map(Error::getMessage)
                .collect(Collectors.toList())
            );
        }
    }
}
