package pl.pw.cyber.dbaccess.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLogRepository;
import pl.pw.cyber.dbaccess.domain.UserCredentialsGenerator;

import java.time.Clock;

@Configuration
class ApplicationConfig {

    @Bean
    TemporaryDbAccessService temporaryDbAccessService(
      Clock clock,
      UserCredentialsGenerator userCredentialsGenerator,
      DatabaseAccessProvider databaseAccessProvider,
      TemporaryAccessAuditLogRepository accessAuditLogRepository
    ) {
        return new TemporaryDbAccessService(
          clock,
          userCredentialsGenerator,
          databaseAccessProvider,
          accessAuditLogRepository
        );
    }

}
