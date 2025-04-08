package pl.pw.cyber.dbaccess.web

import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.ClockControlAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.LogCaptureAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.TokenGenerationAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.TestTokenBuilder.aToken

class AuthorizationIT extends BaseIT implements
        AccessRequestAbility,
        AddExampleUserAbility,
        ClockControlAbility,
        TokenGenerationAbility,
        LogCaptureAbility {


    def setup() {
        setupLogCapture('pl.pw.cyber.dbaccess.infrastructure.spring.security.JwtAuthFilter')
    }

    def cleanup() {
        cleanupLogCapture()
    }

    def "should allow request if user coming from token subject exists"() {
        expect:
            thereIsUser("some-user")
        and:
            def token = generateToken(aToken()
                    .withSubject("some-user")
            )
        when:
            def response = accessRequestBuilder()
                    .withHeader("Authorization", "Bearer ${token}")
                    .makeRequest()
        then:
            assertThat(response).hasStatusCode(200)
    }

    def "should reject request if user is not in allowlist (403)"() {
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
    }
}
