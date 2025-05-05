package pl.pw.cyber.dbaccess.testing.dsl.builders

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog

import java.time.Instant

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class TemporaryAccessAuditLogBuilder {
    String requestedBy = "user"
    String grantedUsername = "nonexistent_user"
    String targetDatabase = "nonexistent_db"
    String permission = "READ_ONLY"
    Instant grantedAt = MovableClock.getInstance().instant()
    Instant expiresAt = MovableClock.getInstance().instant().minusSeconds(1800)
    boolean revoked = false

    static TemporaryAccessAuditLogBuilder anExpiredInvalidAuditLog() {
        return new TemporaryAccessAuditLogBuilder()
    }

    TemporaryAccessAuditLog build() {
        return TemporaryAccessAuditLog.builder()
                .withRequestedByUsername(requestedBy)
                .withGrantedUsername(grantedUsername)
                .withTargetDatabase(targetDatabase)
                .withPermissionLevel(permission)
                .withGrantedAt(grantedAt)
                .withExpiresAt(expiresAt)
                .withRevoked(revoked)
                .build()
    }
}