package pl.pw.cyber.dbaccess.infrastructure.spring.security

import pl.pw.cyber.dbaccess.testing.dsl.abilities.GenerateKeysAbility
import pl.pw.cyber.dbaccess.testing.dsl.fixtures.JwtTokenFixture
import spock.lang.Specification

import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.text.ParseException

class JwtTokenVerifierSpec extends Specification implements GenerateKeysAbility {

    JwtTokenVerifier verifier
    ECPrivateKey privateKey

    def setup() {
        def keyPair = generateECKeyPair()
        privateKey = keyPair.private as ECPrivateKey
        verifier = new JwtTokenVerifier(keyPair.public as ECPublicKey)
    }

    def "should verify valid token with EC keys"() {
        given:
            def token = JwtTokenFixture.signedWithEC(privateKey, "alice")

        expect:
            def claims = verifier.verify(token)
            claims.subject == "alice"
    }

    def "should fail verification if signature is invalid (wrong key)"() {
        given:
            def attackerKeys = generateECKeyPair()
            def token = JwtTokenFixture.signedWithEC(attackerKeys.private as ECPrivateKey, "eve")

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.cause.message == "Invalid signature"
    }

    def "should fail if token is unsigned"() {
        given:
            def token = JwtTokenFixture.unsigned("bob")

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.cause instanceof ParseException
            ex.cause.message.contains("Not a JWS header")
    }

    def "should fail if token is expired"() {
        given:
            def token = JwtTokenFixture.expired(privateKey, "expired-user")

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.cause.message == "Token expired"
    }

    def "should fail with invalid algorithm (RSA signed)"() {
        given:
            def rsaKey = generateRSAKeyPair()
            def token = JwtTokenFixture.signedWithRSA(rsaKey.private)

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.message == "Invalid token"
    }

    def "should fail with wrong issuer"() {
        given:
            def token = JwtTokenFixture.withIssuer(privateKey, "wrong-issuer", "bad-issuer")

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.cause.message == "Invalid issuer"
    }

    def "should fail with wrong audience"() {
        given:
            def token = JwtTokenFixture.withAudience(privateKey, "wrong-audience", "evil-client")

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.cause.message == "Invalid audience"
    }
}
