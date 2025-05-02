package pl.pw.cyber.dbaccess.adapters.config;

import pl.pw.cyber.dbaccess.common.result.ResultExecutionException;
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase;

class YamlDatabaseConfigurationProvider implements DatabaseConfigurationProvider {

    private static final String DB_URL_ENV_SUFFIX = "_DB_URL";
    private static final String DB_USERNAME_ENV_SUFFIX = "_DB_USERNAME";
    private static final String DB_PASSWORD_ENV_SUFFIX = "_DB_PASSWORD";

    private final DatabaseAccessProperties properties;
    private final EnvironmentReader env;

    YamlDatabaseConfigurationProvider(
      DatabaseAccessProperties properties,
      EnvironmentReader env) {
        this.properties = properties;
        this.env = env;
    }

    @Override
    public boolean isResolvable(String databaseName) {
        try {
            resolve(databaseName);
            return true;
        } catch (ResultExecutionException e) {
            return false;
        }
    }

    @Override
    public ResolvedDatabase resolve(String databaseName) {
        var def = properties.databases().get(databaseName);
        if (def == null) {
            throw new ResultExecutionException.DatabaseNotResolvable("Database definition not found for: " + databaseName);
        }

        String prefix = def.envPrefix();
        String url = env.getEnv(prefix + DB_URL_ENV_SUFFIX);
        String username = env.getEnv(prefix + DB_USERNAME_ENV_SUFFIX);
        String password = env.getEnv(prefix + DB_PASSWORD_ENV_SUFFIX);

        if (url == null || username == null || password == null) {
            throw new ResultExecutionException.DatabaseNotResolvable("Missing DB connection details for: " + databaseName);
        }

        return new ResolvedDatabase(databaseName, url, username, password);
    }
}
