package pl.pw.cyber.dbaccess.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pl.pw.cyber.dbaccess.application.commands.GrantTemporaryAccessCommand;
import pl.pw.cyber.dbaccess.application.results.TemporaryAccessGranted;
import pl.pw.cyber.dbaccess.common.result.Result;
import pl.pw.cyber.dbaccess.domain.CreateTemporaryUserRequest;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.UserCredentialsGenerator;

import java.time.Clock;

@Slf4j
@RequiredArgsConstructor
public class TemporaryDbAccessService {
    private final Clock clock;
    private final UserCredentialsGenerator credentialsGenerator;
    private final DatabaseAccessProvider databaseAccessProvider;

    public Result<TemporaryAccessGranted> accessRequest(GrantTemporaryAccessCommand command) {
        return Result.of(() -> {
            log.info("Attempting to access temporary user {}", command);


            var credentials = credentialsGenerator.generate();
            databaseAccessProvider.createTemporaryUser(
              CreateTemporaryUserRequest.builder()
                .username(credentials.username())
                .password(credentials.password())
                .permissionLevel(command.permissionLevel())
                .build()
            );

            var expiresAt = clock.instant().plus(command.duration());

            return new TemporaryAccessGranted(
              command.targetDatabase(),
              credentials.username(),
              credentials.password(),
              expiresAt
            );
        });

    }
}
