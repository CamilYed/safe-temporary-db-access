package pl.pw.cyber.dbaccess.testing.dsl.builders

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import java.time.Duration
import java.time.Instant

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class TestTokenBuilder {
    private static MovableClock CLOCK = MovableClock.getInstance()
    String subject = "some user"
    String issuer = "dbaccess-api"
    String audience = "dbaccess-client"
    Map<String, Object> claims = [:]
    Instant issueTime = CLOCK.instant()
    Duration ttl = Duration.ofMinutes(5)

    static TestTokenBuilder aToken() {
        return new TestTokenBuilder(MovableClock.getInstance())
    }

    static TestTokenBuilder aToken(MovableClock movableClock) {
        return new TestTokenBuilder(movableClock)
    }

    TestTokenBuilder(MovableClock movableClock) {
        CLOCK = movableClock
    }
}
