package pl.pw.cyber.dbaccess.testing.dsl.abilities


import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder

import static pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder.anAccessRequest

trait AccessRequestAbility implements MakeRequestAbility {

    ResponseEntity<Map> accessRequestBy(String user, Closure<AccessRequestJsonBuilder> config) {
        AccessRequestJsonBuilder requestBuilder = config.call()
        return accessRequest(requestBuilder, user)
    }

    ResponseEntity<Map> accessRequest(AccessRequestJsonBuilder request, String requestedBy = "user") {
        return accessRequestBuilder()
                .withHeader("Authorization", "Bearer ${validToken(requestedBy)}")
                .withBody(request.toMap())
                .makeRequest()
    }

    HttpRequestBuilder accessRequestBuilder() {
        return requestBuilder()
                .withUrl("/access-request")
                .withMethod(HttpMethod.POST)
                .withContentType("application/json")
                .withAccept("application/json")
                .withBody(anAccessRequest().toMap())
    }
}