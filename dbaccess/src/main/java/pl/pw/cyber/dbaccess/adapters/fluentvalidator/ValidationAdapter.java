package pl.pw.cyber.dbaccess.adapters.fluentvalidator;

import br.com.fluentvalidator.AbstractValidator;
import br.com.fluentvalidator.context.Error;
import pl.pw.cyber.dbaccess.web.validation.ValidationResult;
import pl.pw.cyber.dbaccess.web.validation.Validator;

import java.util.List;
import java.util.stream.Collectors;

class ValidationAdapter<T> implements Validator<T> {
    private final AbstractValidator<T> validator;

    public ValidationAdapter(AbstractValidator<T> validator) {
        this.validator = validator;
    }

    public ValidationResult validate(T value) {
        var result = validator.validate(value);
        return result.isValid()
          ? ValidationResult.VALID
          : new ValidationResult.Invalid(mapToErrors(result)
        );
    }

    private static List<String> mapToErrors(br.com.fluentvalidator.context.ValidationResult result) {
        return result.getErrors().stream()
          .map(Error::getMessage)
          .collect(Collectors.toList());
    }
}
