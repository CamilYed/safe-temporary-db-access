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

    ResponseAssertion isOK() {
        return hasStatusCode(200)
    }

    ResponseAssertion hasStatusCode(int expectedStatusCode) {
        assert response.statusCode.value() == expectedStatusCode: "Expected status code ${expectedStatusCode} but was ${response.statusCode.value()}"
        return this
    }

    ResponseAssertion hasValidationError(String expectedMessage) {
        List<String> errors = (response.body?.errors ?: []) as List<String>
        assert errors.contains(expectedMessage): "Expected validation error: '${expectedMessage}', but was: ${errors}"
        return this
    }

    ResponseAssertion hasValidationErrors(List<String> expectedMessages) {
        List<String> errors = (response.body?.errors ?: []) as List<String>
        expectedMessages.each { expected ->
            assert errors.contains(expected): "Expected validation error: '${expected}', but was: ${errors}"
        }
        return this
    }
}
