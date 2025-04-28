package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import org.testcontainers.containers.PostgreSQLContainer
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase
import pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder
import pl.pw.cyber.dbaccess.testing.dsl.builders.TableDefinitionBuilder
import pl.pw.cyber.dbaccess.testing.dsl.fakes.FakeDatabaseConfigurationProvider

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
        if (!startedContainers.containsKey(builder.name)) {
            PostgreSQLContainer container = new PostgreSQLContainer("postgres:16")
                    .withDatabaseName(builder.name)
                    .withUsername("someuser")
                    .withPassword("somepassword")

            container.start()

            println """
            [Testcontainers] ✅ Started PostgreSQL for '${builder.name}'
            → Host: ${container.host}:${container.firstMappedPort}
            → JDBC: ${container.jdbcUrl}
            """

            ResolvedDatabase db = new ResolvedDatabase(
                    builder.name,
                    container.jdbcUrl,
                    container.username,
                    container.password
            )

            databaseConfigurationProvider.add(db)
            startedContainers.put(builder.name, container)
            runningDatabases.put(builder.name, db)
        }
    }

    void publicSchemaOfDatabaseHasTable(String databaseName, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = TableDefinitionBuilder) Closure closure) {
        def container = startedContainers.get(databaseName)
        assert container != null : "No running container for database: $databaseName"

        def builder = TableDefinitionBuilder.table(closure)

        def jdbc = new JdbcTemplate(new DriverManagerDataSource(
                container.jdbcUrl,
                container.username,
                container.password
        ))

        jdbc.execute("CREATE TABLE public.${builder.tableName} (${builder.columns.join(', ')})")

        builder.rows.each { row ->
            def columns = row.keySet().join(', ')
            def values = row.values().collect { "'${it}'" }.join(', ')
            jdbc.execute("INSERT INTO public.${builder.tableName} (${columns}) VALUES (${values})")
        }
    }

    ResolvedDatabase databaseFor(String dbName) {
        def db = runningDatabases.get(dbName)
        if (db == null) {
            throw new IllegalStateException("No database started for name: $dbName")
        }
        return db
    }

    void stopDatabase() {
        startedContainers.each { name, container ->
            println "[Testcontainers] 🔥 Stopping PostgreSQL container: $name"
            container?.stop()
        }
        startedContainers.clear()
        runningDatabases.clear()
    }
}
