package pl.pw.cyber.dbaccess.testing.dsl.abilities


import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

trait AccessRequestAbility implements MakeRequestAbility {

    ResponseEntity<Map> accessRequest(String user) {
        return accessRequestBuilder()
                .withHeader("Authorization", "Bearer ${validToken(user)}")
                .makeRequest()
    }

    HttpRequestBuilder accessRequestBuilder() {
        return requestBuilder()
                .withUrl("/access-request")
                .withMethod(HttpMethod.POST)
                .withContentType("application/json")
                .withAccept("application/json")
                .withBody([:])
    }
}