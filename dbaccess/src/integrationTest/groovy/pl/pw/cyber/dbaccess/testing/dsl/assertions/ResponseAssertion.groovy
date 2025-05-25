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

    static void assertThat(ResponseEntity<Map> response, @DelegatesTo(ResponseAssertion) Closure<?> block) {
        def assertion = new ResponseAssertion(response)
        block.delegate = assertion
        block.resolveStrategy = Closure.DELEGATE_FIRST
        block.call()
    }


    ResponseAssertion isOK() {
        return hasStatusCode(200)
    }

    ResponseAssertion hasStatus(int expectedStatusCode) {
        return hasStatusCode(expectedStatusCode)
    }

    ResponseAssertion hasTitle(String expectedTitle) {
        def actual = response.body?.title
        assert actual == expectedTitle : "Expected title '${expectedTitle}' but was '${actual}'"
        return this
    }

    ResponseAssertion hasDetail(String expectedDetail) {
        def actual = response.body?.detail
        assert actual == expectedDetail : "Expected detail '${expectedDetail}' but was '${actual}'"
        return this
    }

    ResponseAssertion hasType(String expectedType) {
        def actual = response.body?.type
        assert actual == expectedType : "Expected type '${expectedType}' but was '${actual}'"
        return this
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
