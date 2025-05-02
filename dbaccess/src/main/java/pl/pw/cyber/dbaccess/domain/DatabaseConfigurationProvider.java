package pl.pw.cyber.dbaccess.domain;

public interface DatabaseConfigurationProvider {
    boolean isResolvable(String databaseName);

    ResolvedDatabase resolve(String databaseName);
}
