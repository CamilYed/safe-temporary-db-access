package pl.pw.cyber.dbaccess.testing.dsl.abilities

import groovy.json.JsonOutput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import pl.pw.cyber.dbaccess.infrastructure.spring.security.TestJwtTokenGenerator

import static pl.pw.cyber.dbaccess.testing.dsl.builders.TestTokenBuilder.aToken

trait MakeRequestAbility implements ClockControlAbility {

    @Autowired
    private TestRestTemplate restTemplate

    @Autowired
    private TestJwtTokenGenerator testJwtTokenGenerator

    private int port

    void setPort(int port) {
        this.port = port
    }

    String validToken(String user) {
        return testJwtTokenGenerator.generateToken(aToken(testClock()).withSubject(user))
    }

    HttpRequestBuilder requestBuilder() {
        return new HttpRequestBuilder(restTemplate, port)
    }

    HttpRequestBuilder requestedBy(String user) {
        return requestBuilder()
                .withHeader("Authorization", "Bearer ${validToken(user)}")
    }

    static class HttpRequestBuilder {
        private final TestRestTemplate restTemplate
        private final int port
        private String url = ""
        private HttpMethod method = HttpMethod.GET
        private String contentType = MediaType.APPLICATION_JSON_VALUE
        private Map body = null
        private String accept = MediaType.APPLICATION_JSON_VALUE
        private Map<String, String> headers = [:]

        HttpRequestBuilder(TestRestTemplate restTemplate, int port) {
            this.restTemplate = restTemplate
            this.port = port
        }

        HttpRequestBuilder withUrl(String url) {
            this.url = url
            return this
        }

        HttpRequestBuilder withMethod(HttpMethod method) {
            this.method = method
            return this
        }

        HttpRequestBuilder withContentType(String contentType) {
            this.contentType = contentType
            return this
        }

        HttpRequestBuilder withBody(Map body) {
            this.body = body
            return this
        }

        HttpRequestBuilder withAccept(String accept) {
            this.accept = accept
            return this
        }

        HttpRequestBuilder withBasicAuth(String username, String password) {
            String encoded = "${username}:${password}".bytes.encodeBase64().toString()
            return withHeader("Authorization", "Basic ${encoded}")
        }

        HttpRequestBuilder withHeaders(Map<String, String> headers) {
            this.headers.putAll(headers)
            return this
        }

        HttpRequestBuilder withHeader(String key, String value) {
            this.headers.put(key, value)
            return this
        }

        ResponseEntity<Map> makeRequest() {
            HttpHeaders httpHeaders = new HttpHeaders()
            httpHeaders.setContentType(MediaType.valueOf(contentType))
            httpHeaders.setAccept([MediaType.valueOf(accept)])
            headers.each { k, v -> httpHeaders.set(k, v) }

            HttpEntity<Object> entity = new HttpEntity<>(JsonOutput.toJson(body), httpHeaders)
            String fullUrl = "http://localhost:${port}${url}"
            return restTemplate.exchange(fullUrl, method, entity, Map)
        }

        ResponseEntity<String> makeRequestForHtml() {
            HttpHeaders httpHeaders = new HttpHeaders()
            httpHeaders.setContentType(MediaType.valueOf(contentType))
            httpHeaders.setAccept([MediaType.TEXT_HTML])
            headers.each { k, v -> httpHeaders.set(k, v) }

            HttpEntity<Object> entity = new HttpEntity<>(null, httpHeaders)
            String fullUrl = "http://localhost:${port}${url}"
            return restTemplate.exchange(fullUrl, method, entity, String)
        }

        ResponseEntity<String> makeRequestForTextPlain() {
            HttpHeaders httpHeaders = new HttpHeaders()
            httpHeaders.setContentType(MediaType.valueOf(contentType))
            httpHeaders.setAccept([MediaType.TEXT_PLAIN])
            headers.each { k, v -> httpHeaders.set(k, v) }

            HttpEntity<Object> entity = new HttpEntity<>(null, httpHeaders)
            String fullUrl = "http://localhost:${port}${url}"
            return restTemplate.exchange(fullUrl, method, entity, String)
        }
    }
}
