package pl.pw.cyber.dbaccess.web.validation;

import java.util.function.Function;

public interface Validator<T> {

    ValidationResult validate(T object);

    default <R> R validateAndMap(T object, Function<ValidationResult, R> mapper) {
        return mapper.apply(validate(object));
    }
}
