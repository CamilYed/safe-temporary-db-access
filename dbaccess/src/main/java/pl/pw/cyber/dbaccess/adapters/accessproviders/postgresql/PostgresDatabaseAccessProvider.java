package pl.pw.cyber.dbaccess.adapters.accessproviders.postgresql;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import pl.pw.cyber.dbaccess.domain.CreateTemporaryUserRequest;
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider;
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;

class PostgresDatabaseAccessProvider implements DatabaseAccessProvider {

    private final DatabaseConfigurationProvider databaseConfigurationProvider;

    PostgresDatabaseAccessProvider(DatabaseConfigurationProvider databaseConfigurationProvider) {
        this.databaseConfigurationProvider = databaseConfigurationProvider;
    }

    @Override
    public void createTemporaryUser(CreateTemporaryUserRequest request) {
        var resolvedDatabaseOpt = databaseConfigurationProvider.resolve(request.targetDatabase());
        if (resolvedDatabaseOpt.isEmpty()) {
            throw new IllegalArgumentException("Unknown target database: " + request.targetDatabase());
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
            throw new RuntimeException("Failed to create temporary user: " + request.username(), e);
        }
    }

    private void createUser(NamedParameterJdbcTemplate jdbc, CreateTemporaryUserRequest request) {
        var params = new MapSqlParameterSource()
          .addValue("username", request.username())
          .addValue("password", request.password());

        jdbc.update("""
                CREATE ROLE :username WITH LOGIN PASSWORD :password
                """, params);
    }

    private void grantPrivileges(NamedParameterJdbcTemplate jdbc, CreateTemporaryUserRequest request) {
        var params = new MapSqlParameterSource()
          .addValue("username", request.username());

        jdbc.update("""
                GRANT CONNECT ON DATABASE :database TO :username
                """, params.addValue("database", request.targetDatabase()));

        jdbc.update("""
                GRANT USAGE ON SCHEMA public TO :username
                """, params);

        var grantSql = switch (request.permissionLevel()) {
            case READ_ONLY -> "GRANT SELECT ON ALL TABLES IN SCHEMA public TO :username";
            case READ_WRITE -> "GRANT SELECT, INSERT, UPDATE ON ALL TABLES IN SCHEMA public TO :username";
            case DELETE -> "GRANT SELECT, DELETE ON ALL TABLES IN SCHEMA public TO :username";
        };
        jdbc.update(grantSql, params);
    }

    private void configureDefaultPrivileges(NamedParameterJdbcTemplate jdbc, CreateTemporaryUserRequest request) {
        var privileges = switch (request.permissionLevel()) {
            case READ_ONLY -> "SELECT";
            case READ_WRITE -> "SELECT, INSERT, UPDATE";
            case DELETE -> "SELECT, DELETE";
        };

        var params = new MapSqlParameterSource()
          .addValue("username", request.username());

        String sql = """
                ALTER DEFAULT PRIVILEGES IN SCHEMA public
                GRANT %s ON TABLES TO :username
                """.formatted(privileges);

        jdbc.update(sql, params);
    }

    @Override
    public void revokeTemporaryUser(String username, String targetDatabase) {

    }
}
