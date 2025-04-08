package pl.pw.cyber.dbaccess.infrastructure.spring.security

import com.nimbusds.jose.JOSEException
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import groovy.util.logging.Slf4j
import pl.pw.cyber.dbaccess.testing.dsl.builders.TestTokenBuilder

import java.security.interfaces.ECPrivateKey

@Slf4j
class TestJwtTokenGenerator {

    private final ECPrivateKey privateKey

    TestJwtTokenGenerator(ECPrivateKey privateKey) {
        this.privateKey = privateKey
    }

    String generateToken(TestTokenBuilder builder) {
        Date now = Date.from(builder.issueTime)
        Date expiration = new Date(now.getTime() + builder.getTtl().toMillis())

        JWTClaimsSet.Builder claimsBuilder = new JWTClaimsSet.Builder()
                .subject(builder.getSubject())
                .issuer(builder.getIssuer() != null ? builder.getIssuer() : JwtKeyProperties.ISSUER)
                .audience(builder.getAudience() != null ? builder.getAudience() : JwtKeyProperties.AUDIENCE)
                .issueTime(now)
                .expirationTime(expiration)

        builder.getClaims().forEach(claimsBuilder::claim)

        try {
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                    claimsBuilder.build()
            )
            jwt.sign(new ECDSASigner(privateKey))
            return jwt.serialize()
        } catch (JOSEException e) {
            log.error("JWT signing failed: {}", e.getMessage())
            throw new RuntimeException("Failed to sign JWT", e)
        }
    }
}
