package pl.pw.cyber.dbaccess.adapters.config;

import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase;

import java.util.Optional;

class YamlDatabaseConfigurationProvider implements DatabaseConfigurationProvider {

    private static final String DB_URL_ENV_SUFFIX = "_DB_URL";
    private static final String DB_USERNAME_ENV_SUFFIX = "_DB_USERNAME";
    private static final String DB_PASSWORD_ENV_SUFFIX = "_DB_PASSWORD";

    private final DatabaseAccessProperties properties;

    YamlDatabaseConfigurationProvider(DatabaseAccessProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean isResolvable(String databaseName) {
        return resolve(databaseName).isPresent();
    }

    @Override
    public Optional<ResolvedDatabase> resolve(String databaseName) {
        var def = properties.databases().get(databaseName);
        if (def == null) return Optional.empty();

        String prefix = def.envPrefix();
        String url = System.getenv(prefix + DB_URL_ENV_SUFFIX);
        String username = System.getenv(prefix + DB_USERNAME_ENV_SUFFIX);
        String password = System.getenv(prefix + DB_PASSWORD_ENV_SUFFIX);

        if (url == null || username == null || password == null) return Optional.empty();

        return Optional.of(new ResolvedDatabase(databaseName, url, username, password));
    }
}
