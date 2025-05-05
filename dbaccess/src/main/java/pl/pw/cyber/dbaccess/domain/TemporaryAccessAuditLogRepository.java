package pl.pw.cyber.dbaccess.domain;

import java.time.Instant;
import java.util.List;

public interface TemporaryAccessAuditLogRepository {

    void logTemporaryAccess(TemporaryAccessAuditLog temporaryAccessAuditLog);

    List<TemporaryAccessAuditLog> findExpiredAndNotRevoked(Instant now);
}
