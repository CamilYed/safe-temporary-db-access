package pl.pw.cyber.dbaccess.testing.dsl.assertions

import pl.pw.cyber.dbaccess.common.result.Result

class ResultAssertion<T> {

    private final Result<T> result

    private ResultAssertion(Result<T> result) {
        this.result = result
    }

    static <T> ResultAssertion<T> assertThat(Result<T> result) {
        return new ResultAssertion<>(result)
    }

    SuccessAssertion isSuccess() {
        assert result instanceof Result.Success<T> : "Expected Success, but got: ${result.class.simpleName}"
        return new SuccessAssertion((result as Result.Success<T>).value())
    }

    FailureAssertion isFailure() {
        assert result instanceof Result.Failure<T> : "Expected Failure, but got: ${result.class.simpleName}"
        return new FailureAssertion((result as Result.Failure<T>).exception())
    }

    class SuccessAssertion {
        private final T value

        SuccessAssertion(T value) {
            this.value = value
        }

        SuccessAssertion hasValue(T expected) {
            assert value == expected : "Expected value '${expected}', but was '${value}'"
            return this
        }

        SuccessAssertion hasValueSatisfying(Closure<?> check) {
            check.call(value)
            return this
        }
    }

    class FailureAssertion {
        private final Exception cause

        FailureAssertion(Exception cause) {
            this.cause = cause
        }

        FailureAssertion hasCauseInstanceOf(Class<? extends Throwable> type) {
            assert type.isInstance(cause) : "Expected exception of type ${type.simpleName}, but was ${cause.class.simpleName}"
            return this
        }

        FailureAssertion hasCauseMessage(String msg) {
            assert cause.message == msg : "Expected message '${msg}', but was '${cause.message}'"
            return this
        }

        FailureAssertion hasCauseSatisfying(Closure<?> check) {
            check.call(cause)
            return this
        }
    }
}

