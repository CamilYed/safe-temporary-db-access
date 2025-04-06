package pl.pw.cyber.dbaccess.testing.dsl.fixtures

import com.nimbusds.jose.Algorithm
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.ECDSASigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT

import java.security.interfaces.ECPrivateKey
import java.security.interfaces.RSAPrivateKey

/**
 * Helper class for generating signed and unsigned JWTs in integration tests.
 */
class JwtTokenFixture {

    static String signedWithEC(ECPrivateKey privateKey, String subject = "test-user") {
        def claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("dbaccess-api")
                .audience(["dbaccess-client"])
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 5 * 60_000)) // 5 minutes
                .build()

        def jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                claims
        )

        jwt.sign(new ECDSASigner(privateKey))
        return jwt.serialize()
    }

    static String signedWithRSA(RSAPrivateKey privateKey, String subject = "malicious-user") {
        def claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("fake-issuer")
                .audience(["unknown-audience"])
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build()

        def jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                claims
        )

        jwt.sign(new RSASSASigner(privateKey))
        return jwt.serialize()
    }

    static String unsigned(String subject = "unsigned-user") {
        def header = '{"alg":"none"}'.bytes.encodeBase64().toString()
        def payload = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("dbaccess-api")
                .audience(["dbaccess-client"])
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build()
                .toJSONObject()
                .toString()
                .bytes
                .encodeBase64()
                .toString()

        return "${header}.${payload}." // brak podpisu na końcu
    }

    static String expired(ECPrivateKey privateKey, String subject = "expired-user") {
        def now = new Date()
        def expired = new Date(now.getTime() - 60_000) // expired 1 minute ago

        def claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("dbaccess-api")
                .audience(["dbaccess-client"])
                .issueTime(now)
                .expirationTime(expired)
                .build()

        def jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                claims
        )

        jwt.sign(new ECDSASigner(privateKey))
        return jwt.serialize()
    }

    static String withIssuer(ECPrivateKey privateKey, String subject, String issuer) {
        def claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer(issuer)
                .audience(["dbaccess-client"])
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build()

        def jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                claims
        )

        jwt.sign(new ECDSASigner(privateKey))
        return jwt.serialize()
    }

    static String withAudience(ECPrivateKey privateKey, String subject, String audience) {
        def claims = new JWTClaimsSet.Builder()
                .subject(subject)
                .issuer("dbaccess-api")
                .audience([audience])
                .issueTime(new Date())
                .expirationTime(new Date(System.currentTimeMillis() + 60_000))
                .build()

        def jwt = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.ES256).build(),
                claims
        )

        jwt.sign(new ECDSASigner(privateKey))
        return jwt.serialize()
    }
}

