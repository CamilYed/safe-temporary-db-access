package pl.pw.cyber.dbaccess.common.result;

import java.util.function.Function;

public sealed interface Result<T> permits Result.Success, Result.Failure {

    record Success<T>(T value) implements Result<T> {}

    record Failure<T>(ResultExecutionException exception) implements Result<T> {}

    static <T> Result<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Result<T> failure(ResultExecutionException throwable) {
        return new Failure<>(throwable);
    }

    static <T> Result<T> failure(Throwable throwable) {
        if (throwable instanceof ResultExecutionException resultExecutionException) {
            return new Failure<>(resultExecutionException);
        } else {
            return new Failure<>(new ResultExecutionException(throwable));
        }
    }

    static <T> Result<T> of(CheckedSupplier<T> supplier) {
        try {
            return success(supplier.get());
        } catch (ResultExecutionException e) {
            return failure(e);
        } catch (Throwable e) {
            return failure(new ResultExecutionException(e));
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
            case Failure<T> ignored -> defaultValue;
        };
    }

    default T getOrThrow() {
        return switch (this) {
            case Success<T> s -> s.value();
            case Failure<T> f -> throw f.exception();
        };
    }

    default <R> Result<R> map(Function<? super T, ? extends R> mapper) {
        return switch (this) {
            case Success<T> s -> {
                try {
                    yield success(mapper.apply(s.value()));
                } catch (ResultExecutionException e) {
                    yield  failure(e);
                } catch (Throwable e) {
                    yield failure(new ResultExecutionException(e));
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
