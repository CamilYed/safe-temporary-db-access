package pl.pw.cyber.dbaccess.testing.dsl.fakes

import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase

class FakeDatabaseConfigurationProvider implements DatabaseConfigurationProvider {

    private final Map<String, ResolvedDatabase> dbs = [:]

    void add(ResolvedDatabase db) {
        dbs[db.name()] = db
    }

    @Override
    boolean isResolvable(String databaseName) {
        return dbs.containsKey(databaseName)
    }

    @Override
    Optional<ResolvedDatabase> resolve(String databaseName) {
        return Optional.ofNullable(dbs[databaseName])
    }
}