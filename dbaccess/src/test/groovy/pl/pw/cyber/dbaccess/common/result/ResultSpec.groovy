package pl.pw.cyber.dbaccess.common.result


import spock.lang.Specification

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResultAssertion.assertThat

class ResultSpec extends Specification {

    def "should return success with expected value"() {
        when:
            def result = Result.of(() -> "hello")

        then:
            assertThat(result)
                    .isSuccess()
                    .hasValue("hello")
    }

    def "should return failure with expected exception"() {
        given:
            def result = Result.of(() -> { throw new IllegalArgumentException("boom") })

        expect:
            assertThat(result)
                    .isFailure()
                    .hasCauseInstanceOf(IllegalArgumentException)
                    .hasCauseMessage("boom")
    }

    def "map should transform success value"() {
        when:
            def result = Result.success(5).map { it * 2 }

        then:
            assertThat(result)
                    .isSuccess()
                    .hasValue(10)
    }

    def "map should wrap exception into failure"() {
        when:
            def result = Result.success("input").map { throw new RuntimeException("map fail") }

        then:
            assertThat(result)
                    .isFailure()
                    .hasCauseMessage("map fail")
    }

    def "flatMap should transform success to success"() {
        when:
            def result = Result.success("A").flatMap { Result.success(it + "B") }

        then:
            assertThat(result)
                    .isSuccess()
                    .hasValue("AB")
    }

    def "flatMap should convert success to failure when exception thrown"() {
        when:
            def result = Result.success("start").flatMap { throw new IllegalStateException("flat fail") }

        then:
            assertThat(result)
                    .isFailure()
                    .hasCauseInstanceOf(IllegalStateException)
                    .hasCauseMessage("flat fail")
    }

    def "flatMap should preserve original failure"() {
        given:
            def result = Result.failure(new IllegalArgumentException("initial fail"))

        when:
            def flatMapped = result.flatMap { Result.success("nope") }

        then:
            assertThat(flatMapped)
                    .isFailure()
                    .hasCauseMessage("initial fail")
    }

    def "getOrElse should return fallback for failure"() {
        given:
            def result = Result.failure(new RuntimeException("fail"))

        expect:
            result.getOrElse("fallback") == "fallback"
    }

    def "getOrThrow should return value for success"() {
        given:
            def result = Result.success("value")

        expect:
            result.getOrThrow() == "value"
    }

    def "getOrThrow should throw ResultExecutionException for failure"() {
        given:
            def result = Result.failure(new IllegalStateException("oops"))

        when:
            result.getOrThrow()

        then:
            def ex = thrown(ResultExecutionException)
            ex.cause instanceof IllegalStateException
            ex.cause.message == "oops"
    }
}
