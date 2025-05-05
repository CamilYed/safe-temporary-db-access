package pl.pw.cyber.dbaccess.application.commands;

import pl.pw.cyber.dbaccess.domain.PermissionLevel;

import java.time.Duration;

public record GrantTemporaryAccessCommand(
  String requestedBy,
  String targetDatabase,
  PermissionLevel permissionLevel,
  Duration duration
) {
}
