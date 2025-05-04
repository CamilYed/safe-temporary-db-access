package pl.pw.cyber.dbaccess.application.commands;

import lombok.Builder;
import pl.pw.cyber.dbaccess.domain.PermissionLevel;

import java.time.Duration;

@Builder
public record GrantTemporaryAccessCommand(
  String requestedBy,
  String targetDatabase,
  PermissionLevel permissionLevel,
  Duration duration
) {
}
