package pl.pw.cyber.dbaccess.web

import org.springframework.test.context.ActiveProfiles
import pl.pw.cyber.dbaccess.testing.MongoBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.SwaggerRequestAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat

@ActiveProfiles(["prod"])
class SwaggerOtherProfilesIT extends MongoBaseIT implements
        AddExampleUserAbility,
        SwaggerRequestAbility {

    def setup() {
        thereIsUser("user")
    }

   def "swagger should not be available for profiles where is prod on list" () {
       when:
           def response = requestedBy("user")
                   .withUrl("/swagger-ui.html")
                   .makeRequestForHtml()
        then:
            assertThat(response).hasStatusCode(401)
   }
}
