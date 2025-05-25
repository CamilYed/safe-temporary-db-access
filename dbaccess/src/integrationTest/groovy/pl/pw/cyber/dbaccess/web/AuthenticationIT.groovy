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


    def "should reject JWT if subject is null (401)"() {
        given:
            def token = generateToken(aToken()
                    .withSubject(null)
                    .withIssueTime(currentTime())
                    .withTtl(Duration.ofMinutes(5))
            )

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(401)

        and:
            warnLogCaptured("JWT subject is null or blank")

        and:
            metricWasExposed {
                hasName("jwt_missing_subject_total")
                hasTag("subject", "null_or_blank")
                hasValueGreaterThan(0.0)
            }
    }

    def "should reject JWT if subject is blank (401)"() {
        given:
            def token = generateToken(aToken()
                    .withSubject("  ")
                    .withIssueTime(currentTime())
                    .withTtl(Duration.ofMinutes(5))
            )

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(401)

        and:
            warnLogCaptured("JWT subject is null or blank")

        and:
            metricWasExposed {
                hasName("jwt_missing_subject_total")
                hasTag("subject", "null_or_blank")
                hasValueGreaterThan(0.0)
            }
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
            warnLogCaptured("JWT expired:")
        and:
            metricWasExposed {
                hasName("jwt_token_expired_total")
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
            warnLogCaptured("JWT TTL exceeds maximum allowed")
        and:
            metricWasExposed {
                hasName("jwt_token_ttl_too_long_total")
                hasTag("subject", "user-with-long-ttl")
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
            warnLogCaptured("JWT verification failed (parse or unknown error)")
        and:
            metricWasExposed {
                hasName("jwt_malformed_total")
                hasValueGreaterThan(0.0)
            }
    }

    def "should reject JWT if issuer is invalid (401)"() {
        given:
            thereIsUser("issuer-user")
            def token = generateToken(aToken()
                    .withSubject("issuer-user")
                    .withIssuer("invalid-issuer")
                    .withIssueTime(currentTime())
                    .withTtl(Duration.ofMinutes(5))
            )

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(401)

        and:
            warnLogCaptured("JWT issuer invalid")

        and:
            metricWasExposed {
                hasName("jwt_invalid_issuer_total")
                hasTag("subject", "issuer-user")
                hasValueGreaterThan(0.0)
            }
    }

    def "should reject JWT if audience is invalid (401)"() {
        given:
            thereIsUser("audience-user")
            def token = generateToken(aToken()
                    .withSubject("audience-user")
                    .withAudience("not-dbaccess-client")
                    .withIssueTime(currentTime())
                    .withTtl(Duration.ofMinutes(5))
            )

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(401)

        and:
            warnLogCaptured("JWT audience invalid")

        and:
            metricWasExposed {
                hasName("jwt_invalid_audience_total")
                hasTag("subject", "audience-user")
                hasValueGreaterThan(0.0)
            }
    }


}
