package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.PostgreSQLContainer
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase
import pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder
import pl.pw.cyber.dbaccess.testing.dsl.builders.TableDefinitionBuilder
import pl.pw.cyber.dbaccess.testing.dsl.fakes.FakeDatabaseConfigurationProvider

import java.util.concurrent.TimeUnit

import static org.awaitility.Awaitility.await

trait DatabaseSetupAbility {

    @Autowired
    private FakeDatabaseConfigurationProvider databaseConfigurationProvider

    private final Map<String, PostgreSQLContainer> startedContainers = [:]
    private final Map<String, ResolvedDatabase> runningDatabases = [:]


    void thereIs(ResolvedDatabaseBuilder builder) {
        ResolvedDatabase db = builder.build()
        databaseConfigurationProvider.add(db)
    }

    void resolvedDatabaseIsRunning(ResolvedDatabaseBuilder builder) {
        if (!startedContainers.containsKey(builder.databaseName)) {
            String callingMethod = Thread.currentThread().stackTrace[2].methodName
            println "[Testcontainers] Method '${callingMethod}' is starting the database: ${builder.databaseName}"
            PostgreSQLContainer container = new PostgreSQLContainer("postgres:16")
                    .withDatabaseName(builder.databaseName)
                    .withUsername(builder.databaseUser)
                    .withPassword("somepassword")

            container.start()

            println """
            [Testcontainers] ✅ Started PostgreSQL for '${builder.databaseName}'
            → Host: ${container.host}:${container.firstMappedPort}
            → JDBC: ${container.jdbcUrl}
            """

            ResolvedDatabase db = new ResolvedDatabase(
                    builder.databaseName,
                    container.jdbcUrl,
                    container.username,
                    container.password
            )

            databaseConfigurationProvider.add(db)
            startedContainers.put(builder.databaseName, container)
            runningDatabases.put(builder.databaseName, db)
        }
    }

    void currentUserOfDb(String dbName, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = CurrentUserAssertion) Closure closure) {
        def container = startedContainers.get(dbName)
        assert container != null : "Database container '$dbName' not found"

        def jdbc = new JdbcTemplate(new DriverManagerDataSource(
                container.jdbcUrl,
                container.username,
                container.password
        ))

        def currentUser = jdbc.queryForObject("SELECT CURRENT_USER", String)

        def assertion = new CurrentUserAssertion(currentUser)
        closure.delegate = assertion
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()
    }

    void thereIsUserInDatabase(String dbName, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = UserDefinitionBuilder) Closure closure) {
        def builder = new UserDefinitionBuilder()
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        closure.call()

        def container = startedContainers.get(dbName)
        assert container != null : "No running container for database: $dbName"

        def jdbc = new JdbcTemplate(new DriverManagerDataSource(
                container.jdbcUrl,
                container.username,
                container.password
        ))

        jdbc.execute("CREATE ROLE \"${builder.username}\" WITH LOGIN PASSWORD '${builder.password}'")
    }

    void publicSchemaOfDatabaseHasTable(String databaseName, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = TableDefinitionBuilder) Closure closure) {
        def container = startedContainers.get(databaseName)
        assert container != null : "No running container for database: $databaseName"

        TableDefinitionBuilder builder = new TableDefinitionBuilder()
        closure.delegate = builder
        closure.resolveStrategy = Closure.DELEGATE_FIRST
        builder = closure.call()

        def jdbc = new JdbcTemplate(new DriverManagerDataSource(
                container.jdbcUrl,
                container.username,
                container.password
        ))

        jdbc.execute("CREATE TABLE public.${builder.tableName} (${builder.columns.join(', ')})")
        awaitTableVisible(jdbc, "public", builder.tableName)

        builder.rows.each { row ->
            def columns = row.keySet().join(', ')
            def values = row.values().collect { "'${it}'" }.join(', ')
            jdbc.execute("INSERT INTO public.${builder.tableName} (${columns}) VALUES (${values})")
        }
    }

    private void awaitTableVisible(JdbcTemplate jdbc, String schemaName, String tableName) {
        await()
                .atMost(5, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .untilAsserted {
                    Integer count = jdbc.queryForObject(
                            "SELECT COUNT(*) FROM pg_tables WHERE schemaname = ? AND tablename = ?",
                            [schemaName, tableName] as Object[],
                            Integer
                    )
                    assert count == 1 : "Table '${schemaName}.${tableName}' not visible yet in pg_tables!"
                }
    }

    ResolvedDatabase databaseFor(String dbName) {
        def db = runningDatabases.get(dbName)
        if (db == null) {
            throw new IllegalStateException("No database started for name: $dbName")
        }
        return db
    }

    void noAnyResolvableDatabases() {
        stopDatabases()
    }

    void stopDatabases() {
        startedContainers.each { name, container ->
            println "[Testcontainers] 🔥 Stopping PostgreSQL container: $name"
            container?.stop()
        }
        startedContainers.clear()
        runningDatabases.clear()
    }

    static class CurrentUserAssertion {
        private final String actualName

        CurrentUserAssertion(String actualName) {
            this.actualName = actualName
        }

        void hasName(String expectedName) {
            assert actualName == expectedName : "Expected CURRENT_USER to be '$expectedName', but was '$actualName'"
        }
    }

    static class UserDefinitionBuilder {
        String username = "temp_user"
        String password = "secret"

        UserDefinitionBuilder withUsername(String value) {
            this.username = value
            return this
        }

        UserDefinitionBuilder withPassword(String value) {
            this.password = value
            return this
        }
    }
}
