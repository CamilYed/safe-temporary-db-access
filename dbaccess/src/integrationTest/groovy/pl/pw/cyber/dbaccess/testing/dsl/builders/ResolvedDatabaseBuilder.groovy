package pl.pw.cyber.dbaccess.testing.dsl.builders

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class ResolvedDatabaseBuilder {

    String name = "test_db"
    String url = "jdbc:postgresql://localhost:5432/test"
    String username = "user"
    String password = "secret"

    static ResolvedDatabaseBuilder aResolvableDatabase() {
        return new ResolvedDatabaseBuilder()
    }

    ResolvedDatabase build() {
        return new ResolvedDatabase(name, url, username, password)
    }
}
