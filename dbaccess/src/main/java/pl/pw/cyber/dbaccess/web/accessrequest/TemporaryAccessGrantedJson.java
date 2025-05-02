package pl.pw.cyber.dbaccess.web.accessrequest;

import java.time.Instant;

public record TemporaryAccessGrantedJson(
  String targetDatabase,
  String username,
  String password,
  Instant expiresAt
) {
}
