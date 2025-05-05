package pl.pw.cyber.dbaccess.adapters.accessproviders.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.pw.cyber.dbaccess.common.result.ResultExecutionException.DatabaseUnexpectedError;
import pl.pw.cyber.dbaccess.domain.CreateTemporaryUserRequest;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;

import java.util.List;
import java.util.Map;

@Slf4j
class PostgresDatabaseAccessProvider implements DatabaseAccessProvider {

    private final DatabaseConfigurationProvider databaseConfigurationProvider;

    PostgresDatabaseAccessProvider(DatabaseConfigurationProvider databaseConfigurationProvider) {
        this.databaseConfigurationProvider = databaseConfigurationProvider;
    }

    @Override
    public void createTemporaryUser(CreateTemporaryUserRequest request) {
        var resolvedDatabase = databaseConfigurationProvider.resolve(request.targetDatabase());
        var jdbc = JdbcTemplateBuilder.from(resolvedDatabase);

        try {
            createUser(jdbc, request);
            grantPrivileges(jdbc, request);
            configureDefaultPrivileges(jdbc, request);
        } catch (Exception e) {
            log.error("Error creating temporary user", e);
            throw new DatabaseUnexpectedError(e.getMessage());
        }
    }

    private void createUser(NamedParameterJdbcTemplate jdbc, CreateTemporaryUserRequest request) {
        var username = "\"" + request.username() + "\"";
        var password = "'" + request.password().replace("'", "''") + "'";

        var sql = String.format(
          "CREATE ROLE %s WITH LOGIN PASSWORD %s",
          username,
          password
        );

        jdbc.getJdbcTemplate().execute(sql);
    }

    private void grantPrivileges(NamedParameterJdbcTemplate jdbc, CreateTemporaryUserRequest request) {
        var username = "\"" + request.username() + "\"";
        var database = "\"" + request.targetDatabase() + "\"";

        jdbc.getJdbcTemplate().execute(
          String.format("GRANT CONNECT ON DATABASE %s TO %s", database, username)
        );

        jdbc.getJdbcTemplate().execute(
          String.format("GRANT USAGE ON SCHEMA public TO %s", username)
        );

        String tablePrivileges = switch (request.permissionLevel()) {
            case READ_ONLY -> "SELECT";
            case READ_WRITE -> "SELECT, INSERT, UPDATE";
            case DELETE -> "SELECT, DELETE";
        };

        jdbc.getJdbcTemplate().execute(
          String.format("GRANT %s ON ALL TABLES IN SCHEMA public TO %s", tablePrivileges, username)
        );

        jdbc.getJdbcTemplate().execute(
          String.format("GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA public TO %s", username)
        );
    }

    private void configureDefaultPrivileges(NamedParameterJdbcTemplate jdbc, CreateTemporaryUserRequest request) {
        var username = doubleQuote(request.username());

        var privileges = switch (request.permissionLevel()) {
            case READ_ONLY -> "SELECT";
            case READ_WRITE -> "SELECT, INSERT, UPDATE";
            case DELETE -> "SELECT, DELETE";
        };

        var sql = """
          ALTER DEFAULT PRIVILEGES IN SCHEMA public
          GRANT %s ON TABLES TO %s
          """.formatted(privileges, username);

        jdbc.getJdbcTemplate().execute(sql);
    }

    @Override
    public void revokeTemporaryUser(String username, String targetDatabase) {
        try {
            var db = JdbcTemplateBuilder.from(databaseConfigurationProvider.resolve(targetDatabase));
            var jdbc = db.getJdbcTemplate();
            var quotedUser = doubleQuote(username);
            var quotedDb = doubleQuote(targetDatabase);
            var currentUser = jdbc.queryForObject("SELECT CURRENT_USER", String.class);
            var revokeStatements = revokeStatements(currentUser, quotedUser, quotedDb);

            for (var sql : revokeStatements) {
                jdbc.execute(sql);
            }

            var roles = db.queryForList(
              " SELECT rolname FROM pg_catalog.pg_roles WHERE pg_has_role(:username, oid, 'member')",
              Map.of("username", username), String.class
            );

            for (var role : roles) {
                if (!role.equals(currentUser)) {
                    jdbc.execute("REVOKE " + doubleQuote(role) + " FROM " + quotedUser);
                }
            }

            jdbc.execute("DROP ROLE IF EXISTS " + quotedUser);
            log.info("User '{}' revoked and dropped from '{}'", username, targetDatabase);
        } catch (Exception e) {
            log.error("Error revoking user '{}' from '{}'", username, targetDatabase, e);
            throw new DatabaseUnexpectedError("Failed to revoke user: " + e.getMessage());
        }
    }

    private List<String> revokeStatements(String currentUser, String quotedUser, String quotedDb) {
        var quotedCurrentUser = doubleQuote(currentUser);
        return List.of(
          "REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM " + quotedUser,
          "REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public FROM " + quotedUser,
          "REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public FROM " + quotedUser,
          "REVOKE ALL PRIVILEGES ON DATABASE " + quotedDb + " FROM " + quotedUser,
          "REVOKE USAGE ON SCHEMA public FROM " + quotedUser,
          "ALTER DEFAULT PRIVILEGES FOR ROLE " + quotedUser + " IN SCHEMA public REVOKE ALL ON TABLES FROM " + quotedUser,
          "ALTER DEFAULT PRIVILEGES FOR ROLE " + quotedCurrentUser + " IN SCHEMA public REVOKE ALL ON TABLES FROM " + quotedUser,
          "ALTER DEFAULT PRIVILEGES FOR ROLE " + quotedCurrentUser + " IN SCHEMA public REVOKE ALL ON SEQUENCES FROM " + quotedUser,
          "ALTER DEFAULT PRIVILEGES FOR ROLE " + quotedCurrentUser + " IN SCHEMA public REVOKE ALL ON FUNCTIONS FROM " + quotedUser,
          "REASSIGN OWNED BY " + quotedUser + " TO " + quotedCurrentUser
        );
    }

    private String doubleQuote(String value) {
        return "\"" + value + "\"";
    }

}
