package pl.pw.cyber.dbaccess.web

import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.ClockControlAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.DatabaseSetupAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.LogCaptureAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.MetricAssertionAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.TokenGenerationAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder.aResolvableDatabase
import static pl.pw.cyber.dbaccess.testing.dsl.builders.TestTokenBuilder.aToken

class AuthorizationIT extends BaseIT implements
        AccessRequestAbility,
        AddExampleUserAbility,
        ClockControlAbility,
        TokenGenerationAbility,
        LogCaptureAbility,
        DatabaseSetupAbility,
        MetricAssertionAbility {

    def setup() {
        setupLogCapture('pl.pw.cyber.dbaccess.infrastructure.spring.security.JwtAuthFilter')
        thereIs(aResolvableDatabase())
    }

    def cleanup() {
        cleanupLogCapture()
    }

    def "should reject request if user is not in allowlist (403) and increment security metric"() {
        expect:
            userWithNameDoesNotExists("not-existing-user")

        and:
            def token = generateToken(aToken()
                    .withSubject("not-existing-user")
            )

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(403)
        and:
            warnLogCaptured("User: not-existing-user not found")
        and:
            metricWasExposed {
                hasName("jwt_user_not_in_allowlist_total")
                hasValueGreaterThan(0.0)
            }
    }

    def "should reject request if JWT subject is null and increment metric"() {
        given:
            def token = generateToken(aToken().withSubject(null))

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(401)
        and:
            warnLogCaptured("Username is null or blank")
        and:
            metricWasExposed {
                hasName("jwt_missing_subject_total")
                hasValueGreaterThan(0.0)
            }
    }

    def "should reject request if JWT subject is blank and increment metric"() {
        given:
            def token = generateToken(aToken().withSubject(" "))

        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(401)
        and:
            warnLogCaptured("Username is null or blank")
        and:
            metricWasExposed {
                hasName("jwt_missing_subject_total")
                hasValueGreaterThan(0.0)
            }
    }
}
