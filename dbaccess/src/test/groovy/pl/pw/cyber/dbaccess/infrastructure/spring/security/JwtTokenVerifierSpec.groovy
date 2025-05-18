package pl.pw.cyber.dbaccess.infrastructure.spring.security

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import pl.pw.cyber.dbaccess.testing.dsl.abilities.GenerateKeysAbility
import pl.pw.cyber.dbaccess.testing.dsl.builders.MovableClock
import pl.pw.cyber.dbaccess.testing.dsl.fixtures.JwtTokenFixture
import spock.lang.Specification

import java.security.InvalidAlgorithmParameterException
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.text.ParseException
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

import static pl.pw.cyber.dbaccess.testing.dsl.builders.TestTokenBuilder.aToken

class JwtTokenVerifierSpec extends Specification implements GenerateKeysAbility {

    private JwtTokenVerifier verifier
    private ECPrivateKey privateKey
    private static MovableClock CLOCK = new MovableClock(Instant.parse("2025-04-07T12:00:00Z"), ZoneId.of("UTC"))
    private TestJwtTokenGenerator tokenGenerator

    def setup() {
        def keyPair = generateECKeyPair()
        privateKey = keyPair.private as ECPrivateKey
        verifier = new JwtTokenVerifier(CLOCK, keyPair.public as ECPublicKey, new SimpleMeterRegistry())
        tokenGenerator = new TestJwtTokenGenerator(privateKey)
    }

    def "should fail if EC key is too weak"() {
        when:
            def weakKey = generateWeakECKeyPair()
            new JwtTokenVerifier(CLOCK, weakKey.public as ECPublicKey, new SimpleMeterRegistry())

        then:
            def ex = thrown(InvalidAlgorithmParameterException)
            ex.message == "Curve not supported: secp128r1 (1.3.132.0.28)"
    }

    def "should verify valid token with EC keys"() {
        given:
            def token = tokenGenerator.generateToken(aToken()
                    .withSubject("alice")
                    .withIssueTime(CLOCK.instant())
                    .withTtl(Duration.ofMinutes(5))
            )

        when:
            def claims = verifier.verify(token)

        then:
            claims.subject == "alice"
    }

    def "should fail verification if signature is invalid (wrong key)"() {
        given:
            def attackerKeys = generateECKeyPair()
            def attackerGenerator = new TestJwtTokenGenerator(attackerKeys.private as ECPrivateKey)

            def token = attackerGenerator.generateToken(aToken()
                    .withSubject("eve")
                    .withIssueTime(CLOCK.instant())
            )

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.message == "Invalid signature"
    }

    def "should fail if token is unsigned"() {
        given:
            def token = JwtTokenFixture.unsigned()

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.cause instanceof ParseException
            ex.cause.message.contains("Not a JWS header")
    }

    def "should fail if token is expired"() {
        given:
            def expiredTime = CLOCK.instant() - Duration.ofMinutes(2)
            def token = tokenGenerator.generateToken(aToken()
                    .withSubject("expired-user")
                    .withIssueTime(expiredTime)
                    .withTtl(Duration.ofMinutes(1))
            )

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.message == "Token expired"
    }

    def "should fail with invalid algorithm (RSA signed)"() {
        given:
            def rsaKey = generateRSAKeyPair()
            def rsaToken = JwtTokenFixture.signedWithRSA(rsaKey.private as RSAPrivateKey)

        when:
            verifier.verify(rsaToken)

        then:
            def ex = thrown(SecurityException)
            ex.message == "Unsupported JWS algorithm RS256, must be ES256"
    }

    def "should fail with wrong issuer"() {
        given:
            def token = tokenGenerator.generateToken(aToken()
                    .withSubject("wrong-issuer")
                    .withIssuer("bad-issuer")
                    .withIssueTime(CLOCK.instant())
            )

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.message == "Invalid issuer"
    }

    def "should fail with wrong audience"() {
        given:
            def token = tokenGenerator.generateToken(aToken()
                    .withSubject("wrong-audience")
                    .withAudience("evil-client")
                    .withIssueTime(CLOCK.instant())
            )

        when:
            verifier.verify(token)

        then:
            def ex = thrown(SecurityException)
            ex.message == "Invalid audience"
    }
}