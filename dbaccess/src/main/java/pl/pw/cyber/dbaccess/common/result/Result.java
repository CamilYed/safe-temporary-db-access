package pl.pw.cyber.dbaccess.common.result;

import java.util.function.Function;

public sealed interface Result<T> permits Result.Success, Result.Failure {

    record Success<T>(T value) implements Result<T> {}

    record Failure<T>(Throwable exception) implements Result<T> {}

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(Throwable throwable) {
        return new Failure<>(throwable);
    }

    static <T> Result<T> of(CheckedSupplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (Throwable e) {
            return failure(e instanceof ResultExecutionException ? e : new ResultExecutionException(e));
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
            case Success<T> s -> s.value();
            case Failure<T> f -> defaultValue;
        };
    }

    default T getOrThrow() {
        return switch (this) {
            case Success<T> s -> s.value();
            case Failure<T> f -> throw new RuntimeException("Execution failed", f.exception());
        };
    }

    default <R> Result<R> map(Function<? super T, ? extends R> mapper) {
        return switch (this) {
            case Success<T> s -> {
                try {
                    yield success(mapper.apply(s.value()));
                } catch (Throwable e) {
                    yield failure(e);
                }
            }
            case Failure<T> f -> failure(f.exception());
        };
    }

    default <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
        return switch (this) {
            case Success<T> s -> {
                try {
                    yield mapper.apply(s.value());
                } catch (Throwable e) {
                    yield failure(e);
                }
            }
            case Failure<T> f -> failure(f.exception());
        };
    }

    @FunctionalInterface
    interface CheckedSupplier<T> {
        T get() throws ResultExecutionException;
    }
}
