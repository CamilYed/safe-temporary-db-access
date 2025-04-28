package pl.pw.cyber.dbaccess.domain;

import lombok.Builder;

@Builder
public record CreateTemporaryUserRequest(
  String username,
  String password,
  PermissionLevel permissionLevel,
  String targetDatabase
) {
}
