package pl.pw.cyber.dbaccess.adapters.config

import pl.pw.cyber.dbaccess.common.result.ResultExecutionException
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase
import pl.pw.cyber.dbaccess.testing.dsl.builders.EnvVariableDatabaseBuilder
import spock.lang.Specification

import static pl.pw.cyber.dbaccess.testing.dsl.builders.EnvVariableDatabaseBuilder.aDatabaseEnv

class YamlDatabaseConfigurationProviderSpec extends Specification {

    private static class FakeEnvironmentReader implements EnvironmentReader {
        private final Map<String, String> env = [:]

        void set(String key, String value) {
            env.put(key, value)
        }

        @Override
        String getEnv(String key) {
            return env.get(key)
        }
    }

    private static YamlDatabaseConfigurationProvider providerFor(String dbName, FakeEnvironmentReader envReader) {
        def props = new DatabaseAccessProperties(
                Map.of(dbName, new DatabaseAccessProperties.DataSourceDefinition(dbName))
        )
        return new YamlDatabaseConfigurationProvider(props, envReader)
    }

    private static FakeEnvironmentReader readerFor(EnvVariableDatabaseBuilder db) {
        def reader = new FakeEnvironmentReader()
        if (db.getUrl() != null)      reader.set("${db.getName()}_DB_URL", db.getUrl())
        if (db.getUsername() != null) reader.set("${db.getName()}_DB_USERNAME", db.getUsername())
        if (db.getPassword() != null) reader.set("${db.getName()}_DB_PASSWORD", db.getPassword())
        return reader
    }

    def "should resolve database when defined and env vars are present"() {
        given:
            def db = aDatabaseEnv().withName("test_db")
            def provider = providerFor("test_db", readerFor(db))

        when:
            ResolvedDatabase result = provider.resolve("test_db")

        then:
            with(result) {
                name() == "test_db"
                url() == db.url
                username() == db.username
                password() == db.password
            }
    }

    def "should throw if database is missing from properties"() {
        given:
            def provider = new YamlDatabaseConfigurationProvider(
                    new DatabaseAccessProperties(Map.of()), new FakeEnvironmentReader()
            )

        when:
            provider.resolve("missing")

        then:
            thrown(ResultExecutionException.DatabaseNotResolvable)
    }

    def "should throw if DB_URL is missing"() {
        given:
            def db = aDatabaseEnv().withName("test_db").withUrl(null)
            def provider = providerFor("test_db", readerFor(db))

        when:
            provider.resolve("test_db")

        then:
            thrown(ResultExecutionException.DatabaseNotResolvable)
    }

    def "should throw if DB_USERNAME is missing"() {
        given:
            def db = aDatabaseEnv().withName("test_db").withUsername(null)
            def provider = providerFor("test_db", readerFor(db))

        when:
            provider.resolve("test_db")

        then:
            thrown(ResultExecutionException.DatabaseNotResolvable)
    }

    def "should throw if DB_PASSWORD is missing"() {
        given:
            def db = aDatabaseEnv().withName("test_db").withPassword(null)
            def provider = providerFor("test_db", readerFor(db))

        when:
            provider.resolve("test_db")

        then:
            thrown(ResultExecutionException.DatabaseNotResolvable)
    }

    def "isResolvable returns true when all env vars exist"() {
        given:
            def db = aDatabaseEnv().withName("prod")
            def provider = providerFor("prod", readerFor(db))

        expect:
            provider.isResolvable("prod")
    }

    def "isResolvable returns false when any env var is missing"() {
        given:
            def db = aDatabaseEnv().withName("prod").withPassword(null)
            def provider = providerFor("prod", readerFor(db))

        expect:
            !provider.isResolvable("prod")
    }

    def "isResolvable returns false when not defined in config"() {
        given:
            def provider = new YamlDatabaseConfigurationProvider(
                    new DatabaseAccessProperties(Map.of()), new FakeEnvironmentReader()
            )

        expect:
            !provider.isResolvable("not_configured")
    }
}
