package pl.pw.cyber.dbaccess.infrastructure.spring.security

import org.springframework.core.io.ByteArrayResource
import pl.pw.cyber.dbaccess.testing.dsl.abilities.GenerateKeysAbility
import spock.lang.Specification

import java.security.InvalidKeyException
import java.security.spec.InvalidKeySpecException
import java.time.Clock

class JwtTokenConfigSpec extends Specification implements GenerateKeysAbility {

    def "should create JwtTokenVerifier from valid DER encoded EC public key"() {
        given:
            def ecKey = generateECKeyPair()
            def encoded = ecKey.public.encoded
            def resource = new ByteArrayResource(encoded)
            def props = JwtKeyProperties.of(resource)
            def config = new JwtTokenConfig()

        when:
            def verifier = config.jwtTokenVerifier(Clock.systemUTC(), props)

        then:
            verifier != null
    }

    def "should fail if public key is PEM-encoded instead of DER"() {
        given:
            def pem = '''-----BEGIN PUBLIC KEY-----
        MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEQXZ5Sc2RQjzhL5UvM7AFZJ+89Ah1nljz
        g5rIbEl/6ghgU0aKcF+TkMJ3Ya5qLzoaVsc6NeVfaFgHTvUcZoTxTw==
        -----END PUBLIC KEY-----'''
            def resource = new ByteArrayResource(pem.bytes)
            def props = JwtKeyProperties.of(resource)
            def config = new JwtTokenConfig()

        when:
            config.jwtTokenVerifier(Clock.systemUTC(), props)

        then:
            def ex = thrown(InvalidKeySpecException)
            ex.cause instanceof InvalidKeyException
    }

    def "should fail with empty key resource"() {
        given:
            def emptyResource = new ByteArrayResource(new byte[0])
            def props = JwtKeyProperties.of(emptyResource)
            def config = new JwtTokenConfig()

        when:
            config.jwtTokenVerifier(Clock.systemUTC(), props)

        then:
            def ex = thrown(InvalidKeySpecException)
            ex.cause instanceof InvalidKeyException
    }

    def "should log error if key decoding fails (example: corrupted content)"() {
        given:
            def badBytes = "not a real key".bytes
            def resource = new ByteArrayResource(badBytes)
            def props = JwtKeyProperties.of(resource)
            def config = new JwtTokenConfig()

        when:
            config.jwtTokenVerifier(Clock.systemUTC(), props)

        then:
            def ex = thrown(InvalidKeySpecException)
            ex.cause instanceof InvalidKeyException
    }
}
