package pl.pw.cyber.dbaccess.web

import pl.pw.cyber.dbaccess.testing.MongoBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.PrometheusUserAbility

class PrometheusIT extends MongoBaseIT implements PrometheusUserAbility {

    def "should allow prometheus user to access metrics endpoint with basic auth"() {
        given:
            withPrometheusUser("prometheus", "secret123")
        when:
            def response = requestBuilder()
                    .withBasicAuth("prometheus", "secret123")
                    .withUrl("/actuator/prometheus")
                    .makeRequestForTextPlain()

        then:
           response.statusCode.value() == 200
    }
}

