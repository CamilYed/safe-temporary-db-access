package pl.pw.cyber.dbaccess.testing.dsl.builders


import pl.pw.cyber.dbaccess.domain.ResolvedDatabase

class ResolvedDatabaseBuilder {

    String name = "test_db"

    ResolvedDatabaseBuilder withName(String name) {
        this.name = name
        return this
    }

    static ResolvedDatabaseBuilder aResolvableDatabase() {
        return new ResolvedDatabaseBuilder()
    }


    ResolvedDatabase build(
            String url = "jdbc:postgresql://localhost:5432/${name}",
            String username = "user",
            String password = "password"
    ) {
        return new ResolvedDatabase(name, url, username, password)
    }
}
