package pl.pw.cyber.dbaccess.testing.dsl.fakes

import pl.pw.cyber.dbaccess.common.result.ResultExecutionException
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
    ResolvedDatabase resolve(String databaseName) {
        ResolvedDatabase db = dbs[databaseName]
        if (db == null) {
            throw new ResultExecutionException.DatabaseUnexpectedError("Fake DB not found for: " + databaseName)
        }
        return db
    }
}