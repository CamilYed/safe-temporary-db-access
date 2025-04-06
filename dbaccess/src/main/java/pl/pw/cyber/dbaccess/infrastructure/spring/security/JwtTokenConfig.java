package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import pl.pw.cyber.dbaccess.domain.UserRepository;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;

@Configuration
class JwtTokenConfig {

    @Bean
    JwtTokenVerifier jwtTokenVerifier(JwtKeyProperties props) throws Exception {
        return new JwtTokenVerifier(loadPublicKey(props.publicKey()));
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
      JwtTokenVerifier jwtTokenVerifier, UserRepository userRepository
    ) {
        return new JwtAuthenticationFilter(jwtTokenVerifier, userRepository);
    }

    private ECPublicKey loadPublicKey(Resource res) throws Exception {
        var cf = CertificateFactory.getInstance("X.509");
        try (InputStream in = res.getInputStream()) {
            var cert = (X509Certificate) cf.generateCertificate(in);
            return (ECPublicKey) cert.getPublicKey();
        }
    }
}
