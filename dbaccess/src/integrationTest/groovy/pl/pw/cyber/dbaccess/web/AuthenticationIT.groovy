package pl.pw.cyber.dbaccess.web


import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.ClockControlAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.LogCaptureAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.MetricAssertionAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.TokenGenerationAbility

import java.time.Duration

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.TestTokenBuilder.aToken

class AuthenticationIT extends BaseIT implements
        AccessRequestAbility,
        AddExampleUserAbility,
        ClockControlAbility,
        TokenGenerationAbility,
        LogCaptureAbility,
        MetricAssertionAbility {


    def setup() {
        setupLogCapture('pl.pw.cyber.dbaccess.infrastructure.spring.security.JwtTokenVerifier')
    }

    def cleanup() {
        cleanupLogCapture()
    }

    def "should reject request if JWT is missing (401)"() {
        when:
            def response = accessRequestBuilder()
                    .withHeaders([:])
                    .makeRequest()
        then:
            assertThat(response).hasStatusCode(401)
    }

    def "should reject request if JWT is expired (401)"() {
        given:
            currentTimeIs("2025-04-07T12:00:00Z")
        and:
            def token = generateToken(aToken()
                    .withIssueTime(currentTime())
                    .withTtl(Duration.ofMinutes(1))
            )
        and:
            timeElapsed(Duration.ofMinutes(10))
        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()
        then:
            assertThat(response).hasStatusCode(401)
        and:
            warnLogCaptured("JWT verification failed: Token expired")
        and:
            metricWasExposed {
                hasName("security_jwt_verification_failed_total")
                hasTag("reason", "expired")
                hasValueGreaterThan(0.0)
            }

    }

    def "should reject request if JWT TTL exceeds maximum allowed (401)"() {
        given:
            currentTimeIs("2025-04-07T12:00:00Z")
            thereIsUser("user-with-long-ttl")
        and:
            def token = generateToken(aToken()
                    .withSubject("user-with-long-ttl")
                    .withIssueTime(currentTime())
                    .withTtl(Duration.ofMinutes(10))
            )

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(401)
        and:
            warnLogCaptured("JWT TTL exceeds the maximum allowed")
        and:
            metricWasExposed {
                hasName("security_jwt_verification_failed_total")
                hasTag("reason", "expired")
                hasValueGreaterThan(0.0)
            }

    }

    def "should reject request if JWT is invalid (401)"() {
        given:
            def invalidToken = "invalid.token.value"

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${invalidToken}")
                    .makeRequest()
        then:
            assertThat(response).hasStatusCode(401)
        and:
            warnLogCaptured("JWT verification failed: Invalid JWS header: Invalid JSON object")
        and:
            metricWasExposed {
                hasName("security_jwt_verification_failed_total")
                hasTag("reason", "expired")
                hasValueGreaterThan(0.0)
            }

    }
}
