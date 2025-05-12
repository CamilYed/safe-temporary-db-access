package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import pl.pw.cyber.dbaccess.domain.UserRepository;

import java.security.KeyFactory;
import java.security.interfaces.ECPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Clock;

@Configuration
class JwtTokenConfig {

    @Bean
    JwtTokenVerifier jwtTokenVerifier(
      Clock clock,
      JwtKeyProperties props,
      MeterRegistry meterRegistry
    ) throws Exception {
        return new JwtTokenVerifier(clock, loadPublicKey(props.publicKey()), meterRegistry);
    }

    @Bean
    public JwtAuthFilter jwtAuthenticationFilter(
      JwtTokenVerifier jwtTokenVerifier, UserRepository userRepository, MeterRegistry meterRegistry
    ) {
        return new JwtAuthFilter(jwtTokenVerifier, userRepository, meterRegistry);
    }

    /**
     * Loads an EC public key from the given resource.
     * The key must be in DER-encoded X.509 format (binary),
     * not PEM (Base64 with BEGIN/END headers).
     *
     * @param res resource pointing to the DER-encoded EC public key
     * @return ECPublicKey loaded from the resource
     * @throws Exception if key parsing fails
     */
    private ECPublicKey loadPublicKey(Resource res) throws Exception {
        byte[] keyBytes = res.getInputStream().readAllBytes();
        KeyFactory kf = KeyFactory.getInstance("EC");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        return (ECPublicKey) kf.generatePublic(keySpec);
    }
}
