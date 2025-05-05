package pl.pw.cyber.dbaccess.infrastructure.spring


import pl.pw.cyber.dbaccess.testing.MongoBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.MakeRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.TokenGenerationAbility

import static org.springframework.http.HttpMethod.POST
import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.TestTokenBuilder.aToken

class GlobalExceptionHandlerIntegrationIT extends MongoBaseIT implements
        MakeRequestAbility,
        TokenGenerationAbility,
        AddExampleUserAbility {

    def setup() {
        thereIsUser("user-1")
    }

    def "should return 400 when IllegalArgumentException is thrown"() {
        given:
            def token = generateToken(aToken().withSubject("user-1"))

        when:
            def response = requestBuilder()
                    .withUrl("/fail/illegal")
                    .withHeader("Authorization", "Bearer ${token}")
                    .withMethod(POST)
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(400)
        and:
            response.body.title == "Bad Request"
            response.body.detail == "Invalid input or argument"
    }

    def "should return 500 when unexpected Throwable is thrown"() {
        given:
            def token = generateToken(aToken().withSubject("user-1"))

        when:
            def response = requestBuilder()
                    .withUrl("/fail/throwable")
                    .withHeader("Authorization", "Bearer ${token}")
                    .withMethod(POST)
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(500)
        and:
            response.body.title == "Internal Server Error"
            response.body.detail == "Unexpected processing error"
    }

    def "should return 500 when ResultExecutionException is thrown"() {
        given:
            def token = generateToken(aToken().withSubject("user-1"))

        when:
            def response = requestBuilder()
                    .withUrl("/fail/result-execution-exception")
                    .withHeader("Authorization", "Bearer ${token}")
                    .withMethod(POST)
                    .makeRequest()

        then:
            assertThat(response).hasStatusCode(500)
        and:
            response.body.title == "Internal Server Error"
            response.body.detail == "Unexpected processing error"
    }
}