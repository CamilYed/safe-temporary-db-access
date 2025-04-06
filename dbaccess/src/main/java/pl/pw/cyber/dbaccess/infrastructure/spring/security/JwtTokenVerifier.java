package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import com.nimbusds.jose.crypto.ECDSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.extern.slf4j.Slf4j;

import java.security.interfaces.ECPublicKey;
import java.text.ParseException;
import java.util.Date;

@Slf4j
class JwtTokenVerifier {

    private final ECPublicKey publicKey;

    JwtTokenVerifier(ECPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public JWTClaimsSet verify(String token) {
        try {
            var jwt = SignedJWT.parse(token);
            if (!jwt.verify(new ECDSAVerifier(publicKey))) {
                throw new SecurityException("Invalid signature");
            }

            return extractJwtClaimsSet(jwt);
        } catch (Exception e) {
            log.warn("JWT verification failed: {}", e.getMessage());
            throw new SecurityException("Invalid token", e);
        }
    }

    private static JWTClaimsSet extractJwtClaimsSet(SignedJWT jwt) throws ParseException {
        var claims = jwt.getJWTClaimsSet();
        var now = new Date();

        if (claims.getExpirationTime().before(now)) {
            throw new SecurityException("Token expired");
        }

        if (!JwtKeyProperties.ISSUER.equals(claims.getIssuer())) {
            throw new SecurityException("Invalid issuer");
        }

        if (!claims.getAudience().contains(JwtKeyProperties.AUDIENCE)) {
            throw new SecurityException("Invalid audience");
        }
        return claims;
    }
}
