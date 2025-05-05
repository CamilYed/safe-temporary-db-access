package pl.pw.cyber.dbaccess.infrastructure.spring.security

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.testing.MongoBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.fixtures.JwtTokenFixture

import java.security.interfaces.ECPrivateKey

class JwtTokenVerifierIntegrationIT extends MongoBaseIT {

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
