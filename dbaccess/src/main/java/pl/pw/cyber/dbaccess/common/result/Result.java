package pl.pw.cyber.dbaccess.common.result;

import java.util.function.Function;

public sealed interface Result<T> permits Result.Success, Result.Failure {

    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(Exception exception) implements Result<T> {}

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(Exception exception) {
        return new Failure<>(exception);
    }

    static <T> Result<T> of(CheckedSupplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (Exception e) {
            return failure(e);
        }
    }

    default boolean isSuccess() {
        return switch (this) {
            case Success<T> ignored -> true;
            case Failure<T> ignored -> false;
        };
    }

    default boolean isFailure() {
        return !isSuccess();
    }

    default T getOrElse(T defaultValue) {
        return switch (this) {
            case Success<T>(T value) -> value;
            case Failure<T> ignored -> defaultValue;
        };
    }

    default T getOrThrow() {
        return switch (this) {
            case Success<T>(T value) -> value;
            case Failure<T>(Exception exception) -> throw new ResultExecutionException(exception);
        };
    }

    default <R> Result<R> map(Function<? super T, ? extends R> mapper) {
        return switch (this) {
            case Success<T>(T value) -> {
                try {
                    yield success(mapper.apply(value));
                } catch (Exception e) {
                    yield failure(e);
                }
            }
            case Failure<T>(Exception exception) -> failure(exception);
        };
    }

    default <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
        return switch (this) {
            case Success<T>(T value) -> {
                try {
                    yield mapper.apply(value);
                } catch (Exception e) {
                    yield failure(e);
                }
            }
            case Failure<T>(Exception exception) -> failure(exception);
        };
    }

    @FunctionalInterface
    interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
