package pl.pw.cyber.dbaccess.adapters.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;

@Configuration
@PropertySource(value = "classpath:db-access.yaml", factory = YamlPropertySourceFactory.class)
@EnableConfigurationProperties(DatabaseAccessProperties.class)
class DatabaseAccessConfig {

    @Bean
    DatabaseConfigurationProvider databaseConfigurationProvider(DatabaseAccessProperties props) {
        return new YamlDatabaseConfigurationProvider(props);
    }
}
