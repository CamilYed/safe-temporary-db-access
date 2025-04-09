package pl.pw.cyber.dbaccess.adapters.filedatabase;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pw.cyber.dbaccess.domain.UserRepository;

@Configuration
class FileDataBaseConfig {

    @Bean
    UserRepository userRepository() {
        return new YamlUserRepository("example-users.yaml");
    }
}
