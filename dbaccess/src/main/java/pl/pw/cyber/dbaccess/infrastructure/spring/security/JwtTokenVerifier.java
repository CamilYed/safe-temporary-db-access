package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.micrometer.core.instrument.MeterRegistry;
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
    private final MeterRegistry meterRegistry;

    private static final int MINIMUM_PUBLIC_EC_KEY_LENGTH = 256;
    private static final Duration MAX_TOKEN_TTL = Duration.ofMinutes(5);

    static final String ISSUER = "dbaccess-api";
    static final String AUDIENCE = "dbaccess-client";

    JwtTokenVerifier(Clock clock, ECPublicKey publicKey, MeterRegistry meterRegistry) {
        if (isKeyToWeak(publicKey)) {
            throw new IllegalArgumentException("EC key too weak: minimum 256 bits required");
        }
        this.clock = clock;
        this.publicKey = publicKey;
        this.meterRegistry = meterRegistry;
    }

    private static boolean isKeyToWeak(ECPublicKey publicKey) {
        return publicKey.getParams().getCurve().getField().getFieldSize() < MINIMUM_PUBLIC_EC_KEY_LENGTH;
    }

    public JWTClaimsSet verify(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            if (!jwt.verify(new ECDSAVerifier(publicKey))) {
                log.error("JWT verification failed because ECDSA verification failed");
                recordSecurityFailure("invalid_signature");
                throw new SecurityException("Invalid signature");
            }
            return extractJwtClaimsSet(jwt);
        } catch (Exception e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            recordSecurityFailure("parse_error");
            throw new SecurityException("Invalid token", e);
        }
    }

    private JWTClaimsSet extractJwtClaimsSet(SignedJWT jwt) throws ParseException {
        var claims = jwt.getJWTClaimsSet();
        var now = Date.from(clock.instant());
        log.info("Checking token, current time is: {}", now);

        if (claims.getExpirationTime().before(now)) {
            log.warn("JWT expired: {}", claims.getSubject());
            recordSecurityFailure("expired");
            throw new SecurityException("Token expired");
        }

        var issueTime = claims.getIssueTime();
        var tokenTtl = Duration.between(issueTime.toInstant(), claims.getExpirationTime().toInstant());
        if (tokenTtl.compareTo(MAX_TOKEN_TTL) > 0) {
            log.warn("JWT TTL exceeds the maximum allowed: {}", tokenTtl);
            recordSecurityFailure("ttl_too_long");
            throw new SecurityException("Token TTL too long");
        }

        if (!ISSUER.equals(claims.getIssuer())) {
            log.warn("JWT not issuer: {}", claims.getIssuer());
            recordSecurityFailure("invalid_issuer");
            throw new SecurityException("Invalid issuer");
        }

        if (!claims.getAudience().contains(AUDIENCE)) {
            log.warn("JWT not audience: {}", claims.getAudience());
            recordSecurityFailure("invalid_audience");
            throw new SecurityException("Invalid audience");
        }

        return claims;
    }

    private void recordSecurityFailure(String reason) {
        meterRegistry.counter("security_jwt_verification_failed_total", "reason", reason).increment();
    }
}
