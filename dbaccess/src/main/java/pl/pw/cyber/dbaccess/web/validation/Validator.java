package pl.pw.cyber.dbaccess.web.validation;

import java.util.function.Function;

public interface Validator<T> {

    ValidationResult validate(T object);

    static <R> R map(ValidationResult result, Function<ValidationResult, R> mapper) {
       return mapper.apply(result);
    }
}
