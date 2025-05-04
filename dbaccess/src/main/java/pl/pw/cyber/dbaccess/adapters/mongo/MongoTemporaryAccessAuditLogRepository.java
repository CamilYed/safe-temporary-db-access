package pl.pw.cyber.dbaccess.adapters.mongo;

import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog;
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLogRepository;

import java.time.Instant;
import java.util.List;

class MongoTemporaryAccessAuditLogRepository implements TemporaryAccessAuditLogRepository {

    private final SpringDataMongoAuditLogRepository repository;

    MongoTemporaryAccessAuditLogRepository(SpringDataMongoAuditLogRepository auditLogRepository) {
        this.repository = auditLogRepository;
    }

    @Override
    public void logTemporaryAccess(TemporaryAccessAuditLog temporaryAccessAuditLog) {
        var mongoDoc = mapToMongoDocument(temporaryAccessAuditLog);
        repository.save(mongoDoc);
    }

    @Override
    public List<TemporaryAccessAuditLog> findExpiredAndNotRevoked(Instant now) {
        return repository.findByExpiresAtBeforeAndRevokedFalse(now).stream()
          .map(MongoTemporaryAccessAuditLogRepository::mapToDomain)
          .toList();
    }

    private static MongoTemporaryAccessAuditLog mapToMongoDocument(TemporaryAccessAuditLog domain) {
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

    private static TemporaryAccessAuditLog mapToDomain(MongoTemporaryAccessAuditLog doc) {
        return TemporaryAccessAuditLog.builder()
          .id(doc.id())
          .requestedByUsername(doc.requestedByUsername())
          .grantedUsername(doc.grantedUsername())
          .targetDatabase(doc.targetDatabase())
          .permissionLevel(doc.permissionLevel())
          .grantedAt(doc.grantedAt())
          .expiresAt(doc.expiresAt())
          .revoked(doc.revoked())
          .build();
    }
}
