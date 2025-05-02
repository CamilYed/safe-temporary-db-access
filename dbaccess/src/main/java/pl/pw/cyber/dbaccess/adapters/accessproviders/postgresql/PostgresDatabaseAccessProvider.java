package pl.pw.cyber.dbaccess.adapters.accessproviders.postgresql;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import pl.pw.cyber.dbaccess.common.result.ResultExecutionException;
import pl.pw.cyber.dbaccess.common.result.ResultExecutionException.DatabaseNotResolvable;
import pl.pw.cyber.dbaccess.common.result.ResultExecutionException.DatabaseUnexpectedError;
import pl.pw.cyber.dbaccess.domain.CreateTemporaryUserRequest;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;

@Slf4j
class PostgresDatabaseAccessProvider implements DatabaseAccessProvider {

    private final DatabaseConfigurationProvider databaseConfigurationProvider;

    PostgresDatabaseAccessProvider(DatabaseConfigurationProvider databaseConfigurationProvider) {
        this.databaseConfigurationProvider = databaseConfigurationProvider;
    }

    @Override
    public void createTemporaryUser(CreateTemporaryUserRequest request) {
        var resolvedDatabaseOpt = databaseConfigurationProvider.resolve(request.targetDatabase());
        if (resolvedDatabaseOpt.isEmpty()) {
            throw new DatabaseNotResolvable("Unknown target database: " + request.targetDatabase());
        }
        var resolvedDatabase = resolvedDatabaseOpt.get();
        var dataSource = new DriverManagerDataSource(
          resolvedDatabase.url(),
          resolvedDatabase.username(),
          resolvedDatabase.password()
        );

        var jdbc = new NamedParameterJdbcTemplate(dataSource);

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
        var username = "\"" + request.username() + "\"";

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
        // TODO
    }
}
