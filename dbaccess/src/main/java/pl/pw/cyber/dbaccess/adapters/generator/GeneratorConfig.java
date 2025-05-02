package pl.pw.cyber.dbaccess.adapters.generator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pw.cyber.dbaccess.domain.UserCredentialsGenerator;

@Configuration
class GeneratorConfig {

    @Bean
    UserCredentialsGenerator userCredentialsGenerator() {
        return new SecureUserCredentialsGenerator();
    }
}
