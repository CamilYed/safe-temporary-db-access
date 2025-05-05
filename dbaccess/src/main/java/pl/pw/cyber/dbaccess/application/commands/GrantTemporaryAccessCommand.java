package pl.pw.cyber.dbaccess.application.commands;

import pl.pw.cyber.dbaccess.domain.PermissionLevel;

import java.time.Duration;

public record GrantTemporaryAccessCommand(
  String requestedBy,
  String targetDatabase,
  PermissionLevel permissionLevel,
  Duration duration
) {
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String requestedBy;
        private String targetDatabase;
        private PermissionLevel permissionLevel;
        private Duration duration;

        public Builder requestedBy(String requestedBy) {
            this.requestedBy = requestedBy;
            return this;
        }

        public Builder targetDatabase(String targetDatabase) {
            this.targetDatabase = targetDatabase;
            return this;
        }

        public Builder permissionLevel(PermissionLevel permissionLevel) {
            this.permissionLevel = permissionLevel;
            return this;
        }

        public Builder duration(Duration duration) {
            this.duration = duration;
            return this;
        }

        public GrantTemporaryAccessCommand build() {
            return new GrantTemporaryAccessCommand(
              requestedBy,
              targetDatabase,
              permissionLevel,
              duration
            );
        }
    }
}
