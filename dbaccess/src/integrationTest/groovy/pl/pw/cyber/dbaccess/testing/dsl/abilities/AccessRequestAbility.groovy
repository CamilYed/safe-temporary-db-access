package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity

trait AccessRequestAbility implements MakeRequestAbility {

    ResponseEntity<Map> accessRequest() {
        return makeRequest(
                url: "/access-request",
                method: HttpMethod.POST,
                contentType: "application/json",
                body: Map.of(),
                accept: "application/json",
        )
    }

}