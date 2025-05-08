package pl.pw.cyber.dbaccess.application;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.pw.cyber.dbaccess.application.commands.GrantTemporaryAccessCommand;
import pl.pw.cyber.dbaccess.application.results.TemporaryAccessGranted;
import pl.pw.cyber.dbaccess.common.result.Result;
import pl.pw.cyber.dbaccess.domain.CreateTemporaryUserRequest;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog;
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLogRepository;
import pl.pw.cyber.dbaccess.domain.TemporaryCredentials;
import pl.pw.cyber.dbaccess.domain.UserCredentialsGenerator;

import java.time.Clock;

@Slf4j
@RequiredArgsConstructor
public class TemporaryDbAccessService {

    private final Clock clock;
    private final MeterRegistry meterRegistry;
    private final UserCredentialsGenerator credentialsGenerator;
    private final DatabaseAccessProvider databaseAccessProvider;
    private final TemporaryAccessAuditLogRepository auditLogRepository;

    public Result<TemporaryAccessGranted> accessRequest(GrantTemporaryAccessCommand command) {
        return Result.of(() -> {
              log.info("Granting temporary access to database '{}' for user '{}'", command.targetDatabase(), command.requestedBy());

              var credentials = createTemporaryUser(command);
              var auditLog = logAccessGrant(command, credentials.username());
              return new TemporaryAccessGranted(
                command.targetDatabase(),
                credentials.username(),
                credentials.password(),
                auditLog.expiresAt()
              );
          })
          .onSuccess(() -> countAccessSuccessTotalMetric(command))
          .onFailure(ex -> {
                countAccessFailedTotalMetric(command.targetDatabase());
                return ex;
            }
          );
    }

    private TemporaryCredentials createTemporaryUser(GrantTemporaryAccessCommand command) {
        var credentials = credentialsGenerator.generate();
        var request = CreateTemporaryUserRequest.builder()
          .username(credentials.username())
          .password(credentials.password())
          .permissionLevel(command.permissionLevel())
          .targetDatabase(command.targetDatabase())
          .build();
        databaseAccessProvider.createTemporaryUser(request);
        log.info("Created temporary user '{}' for database '{}'", credentials.username(), command.targetDatabase());
        return credentials;
    }

    private TemporaryAccessAuditLog logAccessGrant(GrantTemporaryAccessCommand command, String username) {
        var grantedAt = clock.instant();
        var expiresAt = grantedAt.plus(command.duration());

        var auditLog = TemporaryAccessAuditLog.builder()
          .withRequestedByUsername(command.requestedBy())
          .withGrantedUsername(username)
          .withTargetDatabase(command.targetDatabase())
          .withPermissionLevel(command.permissionLevel().name())
          .withGrantedAt(grantedAt)
          .withExpiresAt(expiresAt)
          .withRevoked(false)
          .build();

        auditLogRepository.logTemporaryAccess(auditLog);
        log.info("Logged access grant for user '{}' to database '{}'", username, command.targetDatabase());
        return auditLog;
    }

    private void countAccessSuccessTotalMetric(GrantTemporaryAccessCommand command) {
        meterRegistry.counter(
          "access_success_total",
          "database", command.targetDatabase(),
          "permission", command.permissionLevel().name()
        ).increment();
    }

    private void countAccessFailedTotalMetric(String targetDatabase) {
        meterRegistry.counter(
          "access_failed_total",
          "database", targetDatabase,
          "reason", "creation_error"
        ).increment();
    }

    public void revokeExpiredAccess() {
        var now = clock.instant();
        log.info("Running task to revoke expired access at {}", now);

        var expiredLogs = auditLogRepository.findExpiredAndNotRevoked(now);

        if (expiredLogs.isEmpty()) {
            log.info("No expired access to revoke.");
            return;
        }

        log.info("Found {} expired entries", expiredLogs.size());

        for (var logEntry : expiredLogs) {
            revokeAccess(logEntry);
        }

        log.info("Finished revoking expired access.");
    }

    private void revokeAccess(TemporaryAccessAuditLog logEntry) {
        try {
            log.info("Revoking access for '{}' in database '{}' (ID: {})",
              logEntry.grantedUsername(), logEntry.targetDatabase(), logEntry.id()
            );

            databaseAccessProvider.revokeTemporaryUser(logEntry.grantedUsername(), logEntry.targetDatabase());

            var updatedLog = logEntry.withRevoked(true);
            auditLogRepository.logTemporaryAccess(updatedLog);

            log.info("Revoked and updated audit log for '{}' (ID: {})", logEntry.grantedUsername(), logEntry.id());

        } catch (Exception e) {
            log.error(
              "Failed to revoke access for '{}' (ID: {}): {}",
              logEntry.grantedUsername(), logEntry.id(), e.getMessage(), e
            );
            countRevokeFailedTotalMetric(logEntry.targetDatabase());
        }
    }

    private void countRevokeFailedTotalMetric(String targetDatabase) {
        meterRegistry.counter(
          "revoke_failed_total",
          "database", targetDatabase
        ).increment();
    }
}
