package pl.pw.cyber.dbaccess.web;

import org.springframework.http.ResponseEntity;
import pl.pw.cyber.dbaccess.common.result.Result;

import java.util.Map;
import java.util.function.Function;

public final class ResultMappers {

    private ResultMappers() {}

    public static <T> ResponseEntity<?> toResponseEntity(Result<T> result) {
        return toResponseEntity(result, ResponseEntity::ok);
    }

    public static <T> ResponseEntity<?> toResponseEntity(Result<T> result, Function<T, ResponseEntity<?>> onSuccess) {
        return switch (result) {
            case Result.Success<T> success -> onSuccess.apply(success.value());
            case Result.Failure<T> failure -> ResponseEntity.internalServerError().body(
              Map.of("error", failure.exception().getMessage())
            );
        };
    }
}

