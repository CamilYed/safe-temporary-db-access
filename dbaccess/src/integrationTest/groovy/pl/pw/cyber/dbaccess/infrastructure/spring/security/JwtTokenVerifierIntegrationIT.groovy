package pl.pw.cyber.dbaccess.infrastructure.spring.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import pl.pw.cyber.dbaccess.testing.dsl.fixtures.JwtTokenFixture
import spock.lang.Specification

import java.security.interfaces.ECPrivateKey

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = [JwtTokenTestConfig])
class JwtTokenVerifierIntegrationIT extends Specification {

    @Autowired
    private JwtTokenVerifier jwtTokenVerifier

    @Autowired
    private ECPrivateKey testPrivateKey

    def "should verify token signed with matching private key"() {
        given:
            def token = JwtTokenFixture.signedWithEC(testPrivateKey, "integration-user")

        when:
            def claims = jwtTokenVerifier.verify(token)

        then:
            claims.subject == "integration-user"
            claims.issuer == JwtTokenVerifier.ISSUER
            claims.audience.contains(JwtTokenVerifier.AUDIENCE)
    }
}
