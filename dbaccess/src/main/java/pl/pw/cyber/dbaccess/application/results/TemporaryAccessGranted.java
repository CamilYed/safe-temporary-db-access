package pl.pw.cyber.dbaccess.application.results;

import java.time.Instant;

public record TemporaryAccessGranted(
  String targetDatabase,
  String username,
  String password,
  Instant expiresAt
) {
}
