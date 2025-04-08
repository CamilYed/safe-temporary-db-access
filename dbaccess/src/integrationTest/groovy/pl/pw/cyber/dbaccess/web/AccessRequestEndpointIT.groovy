package pl.pw.cyber.dbaccess.web

import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat

class AccessRequestEndpointIT extends BaseIT implements AccessRequestAbility, AddExampleUserAbility {

    def "should get 200"() {
        given:
            thereIsUser("some-user")

        when:
            def response = accessRequest("some-user")

        then:
            assertThat(response).hasStatusCode(200)
    }
}
