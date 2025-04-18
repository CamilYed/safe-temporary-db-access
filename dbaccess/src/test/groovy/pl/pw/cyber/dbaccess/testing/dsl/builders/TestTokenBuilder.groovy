package pl.pw.cyber.dbaccess.testing.dsl.builders

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import java.time.Duration
import java.time.Instant

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class TestTokenBuilder {
    String subject = "some user"
    String issuer = "dbaccess-api"
    String audience = "dbaccess-client"
    Map<String, Object> claims = [:]
    Duration ttl = Duration.ofMinutes(5)
    Instant issueTime = Instant.parse("2025-04-07T12:00:00Z")

    static TestTokenBuilder aToken() {
        return new TestTokenBuilder()
    }
}
