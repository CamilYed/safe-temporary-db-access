package pl.pw.cyber.dbaccess.testing

import groovy.transform.NamedParam
import groovy.transform.NamedVariant
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import pl.pw.cyber.dbaccess.SafeTemporaryDbAccessApplication
import pl.pw.cyber.dbaccess.infrastructure.spring.security.JwtTokenTestConfig
import pl.pw.cyber.dbaccess.testing.dsl.abilities.MakeRequestAbility
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static org.springframework.http.HttpHeaders.ACCEPT
import static org.springframework.http.HttpHeaders.CONTENT_TYPE

@SpringBootTest(
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS,
        webEnvironment = RANDOM_PORT,
        classes = [SafeTemporaryDbAccessApplication, JwtTokenTestConfig]
)
@ActiveProfiles("test")
abstract class BaseIT extends Specification implements MakeRequestAbility {

    @Value('${local.server.port}')
    private int port

    @Autowired
    private TestRestTemplate restTemplate

    @NamedVariant
    ResponseEntity<Map> makeRequest(
            @NamedParam(required = true) String url,
            @NamedParam(required = true) HttpMethod method,
            @NamedParam(required = false) String contentType = null,
            @NamedParam(required = false) Object body = null,
            @NamedParam(required = false) String accept = null,
            @NamedParam(required = false) Map<String, String> headers) {
        def httpHeaders = buildHeaders(contentType, accept, headers)
        return restTemplate.exchange(localUrl(url), method, new HttpEntity<>(body, httpHeaders), Map)
    }

    private static HttpHeaders buildHeaders(String contentType, String accept, Map<String, String> headers) {
        HttpHeaders httpHeaders = new HttpHeaders()
        !contentType ?: httpHeaders.add(CONTENT_TYPE, contentType)
        !accept ?: httpHeaders.add(ACCEPT, accept)
        (headers ?: [:]).forEach { key, value ->
            httpHeaders.add(key as String, value as String)
        }
        return httpHeaders
    }

    private String localUrl(String endpoint) {
        return "http://localhost:$port$endpoint"
    }
}
