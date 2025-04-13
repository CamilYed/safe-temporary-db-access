package pl.pw.cyber.dbaccess.testing.dsl.builders

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class EnvVariableDatabaseBuilder {
    String name = "test_db"
    String url = "jdbc:postgresql://localhost:5432/test"
    String username = "test_user"
    String password = "test_password"

    static EnvVariableDatabaseBuilder aDatabaseEnv() {
        return new EnvVariableDatabaseBuilder()
    }
}
