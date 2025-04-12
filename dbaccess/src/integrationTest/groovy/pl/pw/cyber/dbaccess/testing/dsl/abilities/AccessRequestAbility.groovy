package pl.pw.cyber.dbaccess.testing.dsl.abilities


import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder

import static pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder.anAccessRequest

trait AccessRequestAbility implements MakeRequestAbility {

    ResponseEntity<Map> accessRequest(String user = "user", AccessRequestJsonBuilder request) {
        return accessRequestBuilder()
                .withHeader("Authorization", "Bearer ${validToken(user)}")
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