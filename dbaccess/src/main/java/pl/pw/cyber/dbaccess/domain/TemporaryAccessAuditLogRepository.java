package pl.pw.cyber.dbaccess.domain;

public interface TemporaryAccessAuditLogRepository {

    void logTemporaryAccess(TemporaryAccessAuditLog temporaryAccessAuditLog);
}
