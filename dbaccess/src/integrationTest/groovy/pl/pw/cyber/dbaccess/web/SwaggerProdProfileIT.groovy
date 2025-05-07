package pl.pw.cyber.dbaccess.web


import pl.pw.cyber.dbaccess.testing.ProdBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.SwaggerRequestAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat

class SwaggerProdProfileIT extends ProdBaseIT implements
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
