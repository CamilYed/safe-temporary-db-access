package pl.pw.cyber.dbaccess.adapters.accessproviders.postgresql;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;

@Configuration
class PostgresProviderConfig {

    @Bean
    DatabaseAccessProvider databaseAccessProvider(DatabaseConfigurationProvider databaseConfigurationProvider) {
        return new PostgresDatabaseAccessProvider(databaseConfigurationProvider);
    }
}
