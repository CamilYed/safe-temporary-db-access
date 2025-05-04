package pl.pw.cyber.dbaccess.adapters.accessproviders.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import pl.pw.cyber.dbaccess.common.result.ResultExecutionException.DatabaseUnexpectedError;
import pl.pw.cyber.dbaccess.domain.CreateTemporaryUserRequest;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;

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

    //    @Override
//    public void revokeTemporaryUser(String username, String targetDatabase) {
//        var resolvedDatabase = databaseConfigurationProvider.resolve(targetDatabase);
//        var jdbc = JdbcTemplateBuilder.from(resolvedDatabase);
//
//        try {
//            log.info("Checking privileges for user '{}' on database '{}'", username, targetDatabase);
//
//            // Revoke table privileges dynamically
//            String revokeTablesSQL =
//              "REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM " + doubleQuote(username) + ";";
//            jdbc.update(revokeTablesSQL, Map.of());
//
//            // Revoke sequence privileges dynamically
//            String revokeSequencesSQL =
//              "REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public FROM " + doubleQuote(username) + ";";
//            jdbc.update(revokeSequencesSQL, Map.of());
//
//            // Revoke function privileges dynamically
//            String revokeFunctionsSQL =
//              "REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public FROM " + doubleQuote(username) + ";";
//            jdbc.update(revokeFunctionsSQL, Map.of());
//
//            // Revoke database privileges dynamically
//            String revokeDatabaseSQL =
//              "REVOKE ALL PRIVILEGES ON DATABASE " + targetDatabase + " FROM " + doubleQuote(username) + ";";
//            jdbc.update(revokeDatabaseSQL, Map.of());
//
//            // Revoke default privileges dynamically
//            String revokeDefaultPrivilegesSQL =
//              "ALTER DEFAULT PRIVILEGES FOR ROLE " + doubleQuote(username) +
//                " REVOKE ALL PRIVILEGES ON TABLES FROM " + doubleQuote(username) + ";";
//            jdbc.update(revokeDefaultPrivilegesSQL, Map.of());
//
//            // Revoke role memberships dynamically
//            String revokeRoleMembershipsSQL =
//              "SELECT rolname FROM pg_catalog.pg_roles WHERE pg_has_role(:username, oid, 'member')";
//
//            var roles = jdbc.queryForList(revokeRoleMembershipsSQL, Map.of("username", username), String.class);
//
//            for (String role : roles) {
//                String revokeRoleSQL = "REVOKE " + doubleQuote(role) + " FROM " + doubleQuote(username) + " ;" ;
//                jdbc.update(revokeRoleSQL, Map.of());
//                log.info("Revoked role '{}' from user '{}'", role, username);
//            }
//
//
//            // Finally, drop the role
//            log.info("Dropping user role '{}' from database '{}'", username, targetDatabase);
//            String dropRoleSQL = "DROP ROLE IF EXISTS " + doubleQuote(username);
//            jdbc.update(dropRoleSQL, Map.of());
//
//            log.info("Successfully revoked and dropped user '{}' from database '{}'", username, targetDatabase);
//        } catch (Exception e) {
//            log.error("Unexpected error while revoking user '{}' from database '{}'", username, targetDatabase, e);
//            throw new DatabaseUnexpectedError("Failed to revoke user: " + e.getMessage());
//        }
//    }
//
    @Override
    public void revokeTemporaryUser(String username, String targetDatabase) {
        var resolvedDatabase = databaseConfigurationProvider.resolve(targetDatabase);
        var jdbc = JdbcTemplateBuilder.from(resolvedDatabase);

        try {
            log.info("Revoking user '{}' from database '{}'", username, targetDatabase);

            String someUserSQL = "SELECT CURRENT_USER;";
            String someUser = jdbc.getJdbcTemplate().queryForObject(someUserSQL, String.class);

            // 1. Revoke direct privileges
            jdbc.update("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM " + doubleQuote(username), Map.of());
            jdbc.update("REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public FROM " + doubleQuote(username), Map.of());
            jdbc.update("REVOKE ALL PRIVILEGES ON ALL FUNCTIONS IN SCHEMA public FROM " + doubleQuote(username), Map.of());
            jdbc.update("REVOKE ALL PRIVILEGES ON DATABASE " + targetDatabase + " FROM " + doubleQuote(username), Map.of());
            jdbc.update("REVOKE USAGE ON SCHEMA public FROM " + doubleQuote(username), Map.of());

            // 2. Revoke default privileges granted BY user TO self (for completeness)
            jdbc.update("ALTER DEFAULT PRIVILEGES FOR ROLE " + doubleQuote(username) +
              " IN SCHEMA public REVOKE ALL ON TABLES FROM " + doubleQuote(username), Map.of());

            // 3. Revoke default privileges granted BY someUser TO user (this fixes your error)
            jdbc.getJdbcTemplate().execute(
              "ALTER DEFAULT PRIVILEGES FOR ROLE " + doubleQuote(someUser) +
                " IN SCHEMA public REVOKE ALL ON TABLES FROM " + doubleQuote(username)
            );
            jdbc.getJdbcTemplate().execute(
              "ALTER DEFAULT PRIVILEGES FOR ROLE " + doubleQuote(someUser) +
                " IN SCHEMA public REVOKE ALL ON SEQUENCES FROM " + doubleQuote(username)
            );
            jdbc.getJdbcTemplate().execute(
              "ALTER DEFAULT PRIVILEGES FOR ROLE " + doubleQuote(someUser) +
                " IN SCHEMA public REVOKE ALL ON FUNCTIONS FROM " + doubleQuote(username)
            );

            // 4. Reassign ownership of any objects owned by the temp user
            jdbc.update("REASSIGN OWNED BY " + doubleQuote(username) + " TO " + doubleQuote(someUser), Map.of());

            // 5. Revoke any role memberships
            String queryMemberships = """
            SELECT rolname FROM pg_catalog.pg_roles
            WHERE pg_has_role(:username, oid, 'member')
        """;
            var roles = jdbc.queryForList(queryMemberships, Map.of("username", username), String.class);
            for (String role : roles) {
                if (!role.equals(someUser)) {
                    jdbc.update("REVOKE " + doubleQuote(role) + " FROM " + doubleQuote(username), Map.of());
                    log.info("Revoked role '{}' from user '{}'", role, username);
                }
            }

            // 6. Drop role
            jdbc.update("DROP ROLE IF EXISTS " + doubleQuote(username), Map.of());
            log.info("Successfully revoked and dropped user '{}' from database '{}'", username, targetDatabase);

        } catch (Exception e) {
            log.error("Unexpected error while revoking user '{}' from database '{}'", username, targetDatabase, e);
            throw new DatabaseUnexpectedError("Failed to revoke user: " + e.getMessage());
        }
    }

    private String singleQuote(String value) {
        return "'" + value + "'";
    }

    private String doubleQuote(String value) {
        return "\"" + value + "\"";
    }

}
