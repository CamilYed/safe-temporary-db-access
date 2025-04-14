package pl.pw.cyber.dbaccess.common.result

import spock.lang.Specification

class ResultSpec extends Specification {

    def "should create Success when no exception"() {
        when:
            def result = Result.of(() -> "hello")

        then:
            result instanceof Result.Success
            result.isSuccess()
            !result.isFailure()
            result.getOrElse("default") == "hello"
            result.getOrThrow() == "hello"
    }

    def "should create Failure when exception thrown"() {
        given:
            def ex = new RuntimeException("fail")

        when:
            def result = Result.of(() -> { throw ex })

        then:
            result instanceof Result.Failure
            !result.isSuccess()
            result.isFailure()
            result.getOrElse("fallback") == "fallback"

        when:
            result.getOrThrow()

        then:
            def thrown = thrown(RuntimeException)
            thrown.message == "Execution failed"
            thrown.cause == ex
    }

    def "should map value in success"() {
        given:
            def result = Result.success(2)

        when:
            def mapped = result.map { it * 5 }

        then:
            mapped instanceof Result.Success
            mapped.getOrThrow() == 10
    }

    def "should return failure if exception thrown during map"() {
        given:
            def result = Result.success("value")

        when:
            def mapped = result.map { throw new RuntimeException("map fail") }

        then:
            mapped instanceof Result.Failure
        when:
            mapped.getOrThrow()

        then:
            def thrown = thrown(RuntimeException)
            thrown.message == "Execution failed"
            thrown.cause.message == "map fail"
    }

    def "map should preserve failure"() {
        given:
            def result = Result.failure(new IllegalStateException("error"))

        when:
            def mapped = result.map { it.toString() }

        then:
            mapped instanceof Result.Failure
        when:
            mapped.getOrThrow()

        then:
            thrown(RuntimeException)
    }

    def "flatMap should transform success to another success"() {
        given:
            def result = Result.success("X")

        when:
            def flatMapped = result.flatMap { v -> Result.success(v + "Y") }

        then:
            flatMapped instanceof Result.Success
            flatMapped.getOrThrow() == "XY"
    }

    def "flatMap should catch exception and return failure"() {
        given:
            def result = Result.success("test")

        when:
            def flatMapped = result.flatMap { throw new RuntimeException("flat fail") }

        then:
            flatMapped instanceof Result.Failure
        when:
            flatMapped.getOrThrow()

        then:
            def thrown = thrown(RuntimeException)
            thrown.message == "Execution failed"
            thrown.cause.message == "flat fail"
    }

    def "flatMap should preserve failure"() {
        given:
            def result = Result.failure(new RuntimeException("init fail"))

        when:
            def flatMapped = result.flatMap { Result.success("should not run") }

        then:
            flatMapped instanceof Result.Failure
        when:
            flatMapped.getOrThrow()

        then:
            thrown(RuntimeException)
    }
}
