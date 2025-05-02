package pl.pw.cyber.dbaccess.adapters.mongo;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document("temporaryAccessAuditLogs")
record MongoTemporaryAccessAuditLog(
  @Id
  String id,
  String requestedByUsername,
  @Indexed
  String grantedUsername,
  String targetDatabase,
  String permissionLevel,
  Instant grantedAt,
  @Indexed
  Instant expiresAt,
  @Indexed
  boolean revoked
) {

}
