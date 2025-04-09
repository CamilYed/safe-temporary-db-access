package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;

import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.time.Clock;
import java.time.Duration;
import java.util.Date;

@Slf4j
class JwtTokenVerifier {

    private final Clock clock;
    private final ECPublicKey publicKey;
    private static final int MINIMUM_PUBLIC_EC_KEY_LENGTH = 256;
    private static final Duration MAX_TOKEN_TTL = Duration.ofMinutes(5);

    static final String ISSUER = "dbaccess-api";
    static final String AUDIENCE = "dbaccess-client";

    JwtTokenVerifier(Clock clock, ECPublicKey publicKey) {
        if (isKeyToWeak(publicKey)) {
            throw new IllegalArgumentException("EC key too weak: minimum 256 bits required");
        }
        this.clock = clock;
        this.publicKey = publicKey;
    }

    private static boolean isKeyToWeak(ECPublicKey publicKey) {
        return publicKey.getParams().getCurve().getField().getFieldSize() < MINIMUM_PUBLIC_EC_KEY_LENGTH;
    }

    public JWTClaimsSet verify(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            if (!jwt.verify(new ECDSAVerifier(publicKey))) {
                log.error("JWT verification failed because ECDSA verification failed");
                throw new SecurityException("Invalid signature");
            }

            return extractJwtClaimsSet(jwt);
        } catch (Exception e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            throw new SecurityException("Invalid token", e);
        }
    }

    private JWTClaimsSet extractJwtClaimsSet(SignedJWT jwt) throws ParseException {
        var claims = jwt.getJWTClaimsSet();
        var now = Date.from(clock.instant());

        // Check if the token is expired
        if (claims.getExpirationTime().before(now)) {
            log.warn("JWT expired: {}", claims.getSubject());
            throw new SecurityException("Token expired");
        }

        // Check if the token's TTL exceeds the maximum allowed
        var issueTime = claims.getIssueTime();
        var tokenTtl = Duration.between(issueTime.toInstant(), claims.getExpirationTime().toInstant());
        if (tokenTtl.compareTo(MAX_TOKEN_TTL) > 0) {
            log.warn("JWT TTL exceeds the maximum allowed: {}", tokenTtl);
            throw new SecurityException("Token TTL too long");
        }

        // Additional checks for issuer and audience
        if (!ISSUER.equals(claims.getIssuer())) {
            log.warn("JWT not issuer: {}", claims.getIssuer());
            throw new SecurityException("Invalid issuer");
        }

        if (!claims.getAudience().contains(AUDIENCE)) {
            log.warn("JWT not audience: {}", claims.getAudience());
            throw new SecurityException("Invalid audience");
        }

        return claims;
    }

}
