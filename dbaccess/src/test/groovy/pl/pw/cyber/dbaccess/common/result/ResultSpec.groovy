package pl.pw.cyber.dbaccess.common.result


import spock.lang.Specification

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResultAssertion.assertThat

class ResultSpec extends Specification {

    def "should assert success via DSL"() {
        given:
            def result = Result.success("abc")

        expect:
            assertThat(result)
                    .isSuccess()
                    .hasValue("abc")
    }

    def "should assert success with custom check via DSL"() {
        given:
            def result = Result.success([1, 2, 3])

        expect:
            assertThat(result)
                    .isSuccess()
                    .hasValueSatisfying { list ->
                        assert list instanceof List
                        assert list.size() == 3
                    }
    }

    def "should assert failure via DSL"() {
        given:
            def ex = new IllegalArgumentException("boom")
            def result = Result.failure(ex)

        expect:
            assertThat(result)
                    .isFailure()
                    .hasCauseInstanceOf(IllegalArgumentException)
                    .hasCauseMessage("boom")
    }

    def "should assert failure with satisfying clause"() {
        given:
            def result = Result.failure(new IllegalStateException("fail state"))

        expect:
            assertThat(result)
                    .isFailure()
                    .hasCauseSatisfying { Throwable ex ->
                        assert ex instanceof IllegalStateException
                        assert ex.message == "fail state"
                    }
    }

    def "map should work on success"() {
        given:
            def result = Result.success(2)

        when:
            def mapped = result.map { it * 10 }

        then:
            assertThat(mapped).isSuccess().hasValue(20)
    }

    def "map should catch exception"() {
        given:
            def result = Result.success("x")

        when:
            def mapped = result.map { throw new IllegalArgumentException("map error") }

        then:
            assertThat(mapped)
                    .isFailure()
                    .hasCauseInstanceOf(IllegalArgumentException)
                    .hasCauseMessage("map error")
    }

    def "map should return failure if original is failure"() {
        given:
            def result = Result.failure(new IllegalStateException("fail"))

        when:
            def mapped = result.map { it.toString() }

        then:
            assertThat(mapped).isFailure().hasCauseMessage("fail")
    }

    def "flatMap should transform value"() {
        given:
            def result = Result.success("a")

        when:
            def mapped = result.flatMap { Result.success(it + "b") }

        then:
            assertThat(mapped).isSuccess().hasValue("ab")
    }

    def "flatMap should catch exception"() {
        given:
            def result = Result.success("input")

        when:
            def mapped = result.flatMap { throw new IllegalStateException("fail flat") }

        then:
            assertThat(mapped)
                    .isFailure()
                    .hasCauseMessage("fail flat")
    }

    def "flatMap should return failure if original is failure"() {
        given:
            def result = Result.failure(new IllegalArgumentException("boom"))

        when:
            def mapped = result.flatMap { Result.success("doesn't matter") }

        then:
            assertThat(mapped).isFailure().hasCauseMessage("boom")
    }

    def "getOrElse should return fallback on failure"() {
        given:
            def result = Result.failure(new RuntimeException("fail"))

        expect:
            result.getOrElse("fallback") == "fallback"
    }

    def "getOrElse should return actual value for Success"() {
        expect:
            Result.success("real").getOrElse("fallback") == "real"
    }

    def "getOrThrow should throw if Failure"() {
        given:
            def result = Result.failure(new IllegalStateException("oh no"))

        when:
            result.getOrThrow()

        then:
            def thrown = thrown(ResultExecutionException)
            thrown.message == "java.lang.IllegalStateException: oh no"
            thrown.cause instanceof IllegalStateException
            thrown.cause.message == "oh no"
    }

    def "getOrThrow should return value on Success"() {
        expect:
            Result.success("value").getOrThrow() == "value"
    }

    def "isFailure should return true for Failure"() {
        expect:
            Result.failure(new RuntimeException()).isFailure()
    }

    def "isFailure should return false for Success"() {
        expect:
            !Result.success("ok").isFailure()
    }

    def "of should return success if no exception"() {
        when:
            def result = Result.of(() -> "safe")

        then:
            assertThat(result).isSuccess().hasValue("safe")
    }

    def "of should wrap generic Throwable in ResultExecutionException"() {
        when:
            def result = Result.of(() -> { throw new IllegalArgumentException("boom") })

        then:
            assertThat(result)
                    .isFailure()
                    .hasCauseInstanceOf(IllegalArgumentException)
                    .hasCauseSatisfying { IllegalArgumentException rex ->
                        assert rex.message == "boom"
                    }
    }

    def "of should return failure when ResultExecutionException thrown directly"() {
        when:
            def result = Result.of(() -> { throw new ResultExecutionException("wrapped") })

        then:
            assertThat(result)
                    .isFailure()
                    .hasCauseInstanceOf(ResultExecutionException)
                    .hasCauseMessage("wrapped")
    }

    def "should use ResultExecutionException with message and cause"() {
        given:
            def cause = new IllegalArgumentException("inner")
            def ex = new ResultExecutionException("wrapped", cause)

        expect:
            ex.message == "wrapped"
            ex.cause == cause
    }

    def "should use ResultExecutionException with cause only"() {
        given:
            def cause = new IllegalStateException("inner cause")
            def ex = new ResultExecutionException(cause)

        expect:
            ex.cause == cause
            ex.message == cause.toString()
    }
}
