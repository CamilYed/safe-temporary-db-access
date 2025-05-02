package pl.pw.cyber.dbaccess.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.pw.cyber.dbaccess.application.commands.GrantTemporaryAccessCommand;
import pl.pw.cyber.dbaccess.application.results.TemporaryAccessGranted;
import pl.pw.cyber.dbaccess.common.result.Result;
import pl.pw.cyber.dbaccess.domain.CreateTemporaryUserRequest;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog;
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLogRepository;
import pl.pw.cyber.dbaccess.domain.UserCredentialsGenerator;

import java.time.Clock;

@Slf4j
@RequiredArgsConstructor
public class TemporaryDbAccessService {
    private final Clock clock;
    private final UserCredentialsGenerator credentialsGenerator;
    private final DatabaseAccessProvider databaseAccessProvider;
    private final TemporaryAccessAuditLogRepository auditLogRepository;

    public Result<TemporaryAccessGranted> accessRequest(GrantTemporaryAccessCommand command) {
        return Result.of(() -> {
            log.info("Attempting to access temporary user {}", command);


            var credentials = credentialsGenerator.generate();
            databaseAccessProvider.createTemporaryUser(
              CreateTemporaryUserRequest.builder()
                .username(credentials.username())
                .password(credentials.password())
                .permissionLevel(command.permissionLevel())
                .targetDatabase(command.targetDatabase())
                .build()
            );
            log.info("Successfully created temporary user '{}' in database '{}'", credentials.username(), command.targetDatabase());

            var grantedAt = clock.instant();
            var expiresAt = grantedAt.plus(command.duration());

            TemporaryAccessAuditLog auditLog = TemporaryAccessAuditLog.builder()
              .requestedByUsername(command.requestedBy())
              .grantedUsername(credentials.username())
              .targetDatabase(command.targetDatabase())
              .permissionLevel(command.permissionLevel().name())
              .grantedAt(grantedAt)
              .expiresAt(expiresAt)
              .revoked(false)
              .build();

            auditLogRepository.logTemporaryAccess(auditLog);

            return new TemporaryAccessGranted(
              command.targetDatabase(),
              credentials.username(),
              credentials.password(),
              expiresAt
            );
        });

    }
}
