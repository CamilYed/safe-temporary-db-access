package pl.pw.cyber.dbaccess.infrastructure.spring.security;

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
    JwtTokenVerifier jwtTokenVerifier(Clock clock, JwtKeyProperties props) throws Exception {
        return new JwtTokenVerifier(clock, loadPublicKey(props.publicKey()));
    }

    @Bean
    public JwtAuthFilter jwtAuthenticationFilter(
      JwtTokenVerifier jwtTokenVerifier, UserRepository userRepository
    ) {
        return new JwtAuthFilter(jwtTokenVerifier, userRepository);
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
