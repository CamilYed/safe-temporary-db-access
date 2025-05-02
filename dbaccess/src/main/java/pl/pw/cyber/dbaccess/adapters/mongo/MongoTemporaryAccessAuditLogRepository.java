package pl.pw.cyber.dbaccess.adapters.mongo;

import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog;
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLogRepository;

class MongoTemporaryAccessAuditLogRepository implements TemporaryAccessAuditLogRepository {

    private final SpringDataMongoAuditLogRepository repository;

    MongoTemporaryAccessAuditLogRepository(SpringDataMongoAuditLogRepository auditLogRepository) {
        this.repository = auditLogRepository;
    }

    @Override
    public void logTemporaryAccess(TemporaryAccessAuditLog temporaryAccessAuditLog) {
        MongoTemporaryAccessAuditLog mongoDoc = mapToMongoDocument(temporaryAccessAuditLog);
        repository.save(mongoDoc);
    }

    private MongoTemporaryAccessAuditLog mapToMongoDocument(TemporaryAccessAuditLog domain) {
        return new MongoTemporaryAccessAuditLog(
          domain.id(),
          domain.requestedByUsername(),
          domain.grantedUsername(),
          domain.targetDatabase(),
          domain.permissionLevel(),
          domain.grantedAt(),
          domain.expiresAt(),
          domain.revoked()
        );
    }

}
