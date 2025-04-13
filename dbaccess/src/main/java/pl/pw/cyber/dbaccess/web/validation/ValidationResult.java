package pl.pw.cyber.dbaccess.web.validation;

import org.springframework.http.ProblemDetail;

import java.util.List;

public sealed interface ValidationResult {

   ValidationResult VALID = new Valid();

    record Valid() implements ValidationResult {
    }

    record Invalid(List<String> errors) implements ValidationResult {

        public ProblemDetail toProblemDetail() {
            return ProblemDetailsBuilder.validationError(errors);
        }
    }
}
