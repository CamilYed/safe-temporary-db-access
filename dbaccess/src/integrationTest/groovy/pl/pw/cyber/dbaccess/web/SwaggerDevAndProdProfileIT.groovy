package pl.pw.cyber.dbaccess.web

import org.springframework.test.context.ActiveProfiles
import pl.pw.cyber.dbaccess.testing.DevBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.SwaggerRequestAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat

@ActiveProfiles("prod")
class SwaggerDevAndProdProfileIT extends DevBaseIT implements AddExampleUserAbility, SwaggerRequestAbility {

    def setup() {
        thereIsUser("user")
    }

    def "swagger should NOT be available when both dev and prod are active"() {
        when:
            def response = requestedBy("user")
                    .withUrl("/swagger-ui.html")
                    .makeRequestForHtml()

        then:
            assertThat(response).hasStatusCode(401)
    }
}
