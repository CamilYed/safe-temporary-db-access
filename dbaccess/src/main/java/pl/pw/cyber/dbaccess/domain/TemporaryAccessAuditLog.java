package pl.pw.cyber.dbaccess.domain;

import lombok.Builder;
import lombok.With;

import java.time.Instant;

import java.time.Instant;
import java.util.Objects;

public record TemporaryAccessAuditLog(
  String id,
  String requestedByUsername,
  String grantedUsername,
  String targetDatabase,
  String permissionLevel,
  Instant grantedAt,
  Instant expiresAt,
  boolean revoked
) {

    public static Builder builder() {
        return new Builder();
    }

    public TemporaryAccessAuditLog withRevoked(boolean revoked) {
        return new TemporaryAccessAuditLog(
          this.id,
          this.requestedByUsername,
          this.grantedUsername,
          this.targetDatabase,
          this.permissionLevel,
          this.grantedAt,
          this.expiresAt,
          revoked
        );
    }

    public static class Builder {
        private String id;
        private String requestedByUsername;
        private String grantedUsername;
        private String targetDatabase;
        private String permissionLevel;
        private Instant grantedAt;
        private Instant expiresAt;
        private boolean revoked;

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withRequestedByUsername(String requestedByUsername) {
            this.requestedByUsername = requestedByUsername;
            return this;
        }

        public Builder withGrantedUsername(String grantedUsername) {
            this.grantedUsername = grantedUsername;
            return this;
        }

        public Builder withTargetDatabase(String targetDatabase) {
            this.targetDatabase = targetDatabase;
            return this;
        }

        public Builder withPermissionLevel(String permissionLevel) {
            this.permissionLevel = permissionLevel;
            return this;
        }

        public Builder withGrantedAt(Instant grantedAt) {
            this.grantedAt = grantedAt;
            return this;
        }

        public Builder withExpiresAt(Instant expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder withRevoked(boolean revoked) {
            this.revoked = revoked;
            return this;
        }

        public TemporaryAccessAuditLog build() {
            return new TemporaryAccessAuditLog(
              id,
              requestedByUsername,
              grantedUsername,
              targetDatabase,
              permissionLevel,
              grantedAt,
              expiresAt,
              revoked
            );
        }
    }
}
