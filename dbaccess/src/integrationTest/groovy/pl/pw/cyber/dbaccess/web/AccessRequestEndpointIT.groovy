package pl.pw.cyber.dbaccess.web

import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat

class AccessRequestEndpointIT extends BaseIT implements AccessRequestAbility {

    def "should get 200"() {
        when:
           def response = accessRequest()

        then:
            assertThat(response).hasStatusCode(200)
    }
}
