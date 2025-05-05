package pl.pw.cyber.dbaccess.testing.dsl.builders

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog

import java.time.Instant

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class TemporaryAccessAuditLogBuilder {
    String requestedBy = "user"
    String grantedUsername = "xxxxx"
    String targetDatabase = "test_db"
    String permission = "READ_ONLY"
    Instant grantedAt = MovableClock.getInstance().instant()
    Instant expiresAt = MovableClock.getInstance().instant()
    boolean revoked = false

    static TemporaryAccessAuditLogBuilder anExpiredAuditLog() {
        return new TemporaryAccessAuditLogBuilder()
                .withGrantedAt(MovableClock.getInstance().instant())
                .withExpiresAt(
                        MovableClock.getInstance().instant().minusSeconds(1800)
                )
    }

    static TemporaryAccessAuditLogBuilder anExpiredInvalidAuditLog() {
        return new TemporaryAccessAuditLogBuilder()
                .withGrantedUsername("nonexistent_user")
                .withTargetDatabase("nonexistent_db")
                .withGrantedAt(MovableClock.getInstance().instant())
                .withExpiresAt(
                        MovableClock.getInstance().instant().minusSeconds(1800)
                )
    }

    static TemporaryAccessAuditLogBuilder activeAuditLog() {
        return new TemporaryAccessAuditLogBuilder()
                .withGrantedAt(MovableClock.getInstance().instant())
                .withExpiresAt(
                        MovableClock.getInstance().instant().plusSeconds(50)
                )
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