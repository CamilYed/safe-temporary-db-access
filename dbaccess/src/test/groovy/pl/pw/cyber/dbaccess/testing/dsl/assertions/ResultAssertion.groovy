package pl.pw.cyber.dbaccess.testing.dsl.assertions

import pl.pw.cyber.dbaccess.common.result.Result
import pl.pw.cyber.dbaccess.common.result.ResultExecutionException

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
            def rootCause = unwrapCause(cause)
            assert type.isInstance(rootCause): "Expected exception of type ${type.simpleName}, but was ${rootCause.class.name}"
            return this
        }

        FailureAssertion hasCauseMessage(String msg) {
            def rootCause = unwrapCause(cause)
            assert rootCause.message == msg : "Expected message '${msg}', but was '${rootCause.message}'"
            return this
        }

        FailureAssertion hasCauseSatisfying(Closure<?> verifier) {
            def rootCause = unwrapCause(cause)
            verifier.call(rootCause)
            return this
        }

        private static Throwable unwrapCause(Throwable ex) {
            return ex instanceof ResultExecutionException && ex.cause != null ? ex.cause : ex
        }
    }
}

