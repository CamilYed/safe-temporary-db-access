package pl.pw.cyber.dbaccess.domain;

import java.util.Optional;

public interface DatabaseConfigurationProvider {
    boolean isResolvable(String databaseName);

    Optional<ResolvedDatabase> resolve(String databaseName);
}
