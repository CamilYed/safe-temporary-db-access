package pl.pw.cyber.dbaccess.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;

@Builder
public record TemporaryAccessAuditLog(
  String id,
  String requestedByUsername,
  String grantedUsername,
  String targetDatabase,
  String permissionLevel,
  Instant grantedAt,
  Instant expiresAt,
  @With
  boolean revoked
) {
}
