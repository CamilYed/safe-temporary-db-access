package pl.pw.cyber.dbaccess.testing.dsl.builders


import pl.pw.cyber.dbaccess.domain.ResolvedDatabase

class ResolvedDatabaseBuilder {

    String databaseName = "test_db"
    String databaseUser = "someuser"

    ResolvedDatabaseBuilder databaseName(String name) {
        this.databaseName = name
        return this
    }

    ResolvedDatabaseBuilder databaseUserName(String name) {
        this.databaseUser = name
        return this
    }

    static ResolvedDatabaseBuilder aResolvableDatabase() {
        return new ResolvedDatabaseBuilder()
    }


    ResolvedDatabase build(
            String url = "jdbc:postgresql://localhost:5432/${databaseName}",
            String username = "user",
            String password = "password"
    ) {
        return new ResolvedDatabase(databaseName, url, username, password)
    }
}
