package pl.pw.cyber.dbaccess.adapters.mongo;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLogRepository;

@Configuration
class MongoTemporaryAccessAuditLogConfig {

    @Bean
    TemporaryAccessAuditLogRepository mongoTemporaryAccessAuditLogRepository(
      SpringDataMongoAuditLogRepository repository
    ) {
        return new MongoTemporaryAccessAuditLogRepository(repository);
    }
}
