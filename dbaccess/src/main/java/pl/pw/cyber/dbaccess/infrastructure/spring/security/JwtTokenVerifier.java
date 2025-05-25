package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import com.nimbusds.jose.JOSEException;
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

    private static final Duration MAX_TOKEN_TTL = Duration.ofMinutes(5);

    static final String ISSUER = "dbaccess-api";
    static final String AUDIENCE = "dbaccess-client";

    JwtTokenVerifier(Clock clock, ECPublicKey publicKey, MeterRegistry meterRegistry) {
        this.clock = clock;
        this.publicKey = publicKey;
        this.meterRegistry = meterRegistry;
    }

    public JWTClaimsSet verify(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            if (!jwt.verify(new ECDSAVerifier(publicKey))) {
                log.error("JWT verification failed because ECDSA verification failed");
                meterRegistry.counter("jwt_invalid_signature_total").increment();
                throw new SecurityException("Invalid signature");
            }
            return extractJwtClaimsSet(jwt);

        } catch (JOSEException e) {
            log.error("JWT verification failed (JOSE error): {}", e.getMessage());
            meterRegistry.counter("jwt_invalid_signature_total").increment();
            throw new SecurityException("Unsupported JWS algorithm RS256, must be ES256");
        } catch (ParseException | IllegalArgumentException e) {
            log.warn("JWT verification failed (parse or unknown error): {}", e.getMessage());
            meterRegistry.counter("jwt_malformed_total").increment();
            throw new SecurityException("Invalid token", e);
        }
    }

    private JWTClaimsSet extractJwtClaimsSet(SignedJWT jwt) throws ParseException {
        var claims = jwt.getJWTClaimsSet();

        var subject = claims.getSubject();
        if (subject == null || subject.isBlank()) {
            log.warn("JWT subject is null or blank");
            meterRegistry.counter("jwt_missing_subject_total", "subject", "null_or_blank").increment();
            throw new SecurityException("Missing subject");
        }

        var now = Date.from(clock.instant());
        log.info("Checking token, current time: {}", now);

        if (claims.getExpirationTime().before(now)) {
            log.warn("JWT expired: {}", claims.getSubject());
            meterRegistry.counter("jwt_token_expired_total", "subject", subject).increment();
            throw new SecurityException("Token expired");
        }

        var issueTime = claims.getIssueTime();
        var tokenTtl = Duration.between(issueTime.toInstant(), claims.getExpirationTime().toInstant());

        if (tokenTtl.compareTo(MAX_TOKEN_TTL) > 0) {
            log.warn("JWT TTL exceeds maximum allowed: {}", tokenTtl);
            meterRegistry.counter("jwt_token_ttl_too_long_total", "subject", subject).increment();
            throw new SecurityException("Token TTL too long");
        }

        if (!ISSUER.equals(claims.getIssuer())) {
            log.warn("JWT issuer invalid: {}", claims.getIssuer());
            meterRegistry.counter("jwt_invalid_issuer_total", "subject", subject).increment();
            throw new SecurityException("Invalid issuer");
        }

        if (!claims.getAudience().contains(AUDIENCE)) {
            log.warn("JWT audience invalid: {}", claims.getAudience());
            meterRegistry.counter("jwt_invalid_audience_total", "subject", subject).increment();
            throw new SecurityException("Invalid audience");
        }

        return claims;
    }
}
