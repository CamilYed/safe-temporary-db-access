package pl.pw.cyber.dbaccess.web.accessrequest


import pl.pw.cyber.dbaccess.testing.MongoBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.DatabaseSetupAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.MetricAssertionAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder.anAccessRequest
import static pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder.aResolvableDatabase

class RateLimiterIT extends MongoBaseIT
        implements AccessRequestAbility,
                AddExampleUserAbility,
                MetricAssertionAbility,
                DatabaseSetupAbility {

    def setup() {
        resolvedDatabaseIsRunning(aResolvableDatabase().databaseName("test_db"))
    }

    def "should return 429 after exceeding request rate limit"() {
        given:
        thereIsUser("user")

        when:
        (1..5).each {
            def response = accessRequestBy("user") {
                anAccessRequest().withTargetDatabase("test_db")
            }
            assertThat(response).hasStatusCode(200)
        }

        and:
        def response = accessRequestBy("user") {
            anAccessRequest().withTargetDatabase("test_db")
        }

        then:
        assertThat(response) {
            hasStatus(429)
            hasTitle("Too Many Requests")
            hasDetail("Rate limit exceeded. Try again later.")
            hasType("/errors/rate-limit-exceeded")
        }

        and:
        metricWasExposed {
            hasName("rate_limit_exceeded_total")
            hasTag("subject", "user")
            hasValueGreaterThan(0.0)
        }
    }

    def "should not apply rate limiting for Swagger UI"() {
        when:
        def response = requestBuilder().withUrl("/swagger-ui/index.html").makeRequestForTextPlain()

        then:
        assertThat(response).hasStatus(200)
    }

}
