package pl.pw.cyber.dbaccess.testing.dsl.assertions

import org.springframework.http.ResponseEntity

class ResponseAssertion {

    private final ResponseEntity<Map> response

    private ResponseAssertion(ResponseEntity<Map> response) {
        this.response = response
    }

    static ResponseAssertion assertThat(ResponseEntity<Map> response) {
        return new ResponseAssertion(response)
    }

    ResponseAssertion hasStatusCode(int expectedStatusCode) {
        assert response.statusCode.value() == expectedStatusCode: "Expected status code ${expectedStatusCode} but was ${response.statusCode.value()}"
        return this
    }
}
