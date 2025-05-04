package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.awaitility.Awaitility
import org.postgresql.util.PSQLException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import pl.pw.cyber.dbaccess.domain.DatabaseAccessProvider
import pl.pw.cyber.dbaccess.web.accessrequest.TemporaryAccessGrantedJson

import java.time.Duration

trait RunOperationOnDatabaseAbility implements DatabaseConnectionAbility {

    @Autowired
    private DatabaseAccessProvider databaseAccessProvider

    void connectionToDatabaseSucceeds(String dbName, String username, String password) {
        def db = databaseFor(dbName)
        def jdbc = connect(username, password, db.url())
        jdbc.queryForObject("SELECT 1", Integer)
    }

    void connectionToDatabaseSucceeds(TemporaryAccessGrantedJson credentials) {
        connectionToDatabaseSucceeds(credentials.targetDatabase(), credentials.username(), credentials.password())
    }

    void connectionToDatabaseShouldFail(String dbName, String username, String password) {
        def db = databaseFor(dbName)
        try {
            connect(username, password, db.url())
            assert false : "Expected connection to fail for user ${username} but it succeeded!"
        } catch (Exception e) {
            Throwable cause = e.getCause() ?: e
            assert cause instanceof PSQLException : "Expected PSQLException for failed connection, but got ${cause?.class?.simpleName}"
            PSQLException psqlEx = (PSQLException) cause
            assert ["28P01", "08001", "08006"].contains(psqlEx.getSQLState()) : "Expected SQLState like 28P01 (invalid_password) or 08xxx (connection error), but got ${psqlEx.getSQLState()}"
            println "Connection failed as expected for user ${username} with SQLState ${psqlEx.getSQLState()}"
        }
    }

    void connectionToDatabaseShouldFailWithState(String dbName, String username, String password, String expectedSqlState) {
        def db = databaseFor(dbName)
        try {
            connect(username, password, db.url())
            assert false: "Expected operation to fail with SQLState ${expectedSqlState} but it succeeded!"
        } catch (Exception e) {
            Throwable cause = e.getCause() ?: e
            assert cause instanceof org.postgresql.util.PSQLException : "Expected PSQLException, got ${cause?.class?.simpleName}"
            def psqlEx = (org.postgresql.util.PSQLException) cause
            assert psqlEx.getSQLState() == expectedSqlState : "Expected SQLState ${expectedSqlState}, but got ${psqlEx.getSQLState()}"
            println "Operation failed as expected with SQLState ${psqlEx.getSQLState()}"
        }
    }

    boolean databaseRoleExists(String dbName, String roleName) {
        def db = databaseFor(dbName)
        def adminJdbc = connect(db.username(), db.password(), db.url())
        def count = adminJdbc.queryForObject(
                "SELECT COUNT(*) FROM pg_catalog.pg_roles WHERE rolname = ?", Integer.class, roleName
        )
        return count > 0
    }

    boolean hasActiveSessions(String dbName, String roleName) {
        def db = databaseFor(dbName)
        def adminJdbc = connect(db.username(), db.password(), db.url())
        def count = adminJdbc.queryForObject(
                "SELECT COUNT(*) FROM pg_stat_activity WHERE usename = ?",
                Integer.class,
                roleName
        )
        return count > 0
    }

    boolean databaseHasNoUnexpectedRoles(String dbName) {
        def db = databaseFor(dbName)
        def adminJdbc = connect(db.username(), db.password(), db.url())
        def count = adminJdbc.queryForObject(
                """
            SELECT COUNT(*) FROM pg_catalog.pg_roles
            WHERE rolname != ?
              AND rolname NOT LIKE 'pg_%'
              AND rolname != 'postgres'
            """, Integer.class, db.username()
        )
        return count == 0
    }

    void database(String dbName, @DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DatabaseStateAssertion) Closure assertions) {
        def assertion = new DatabaseStateAssertion(this, dbName)
        assertions.delegate = assertion
        assertions.resolveStrategy = Closure.DELEGATE_FIRST
        assertions.call()
    }


    void eventually(Closure assertion) {
        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(assertion)
    }

    static class DatabaseStateAssertion {
        private final RunOperationOnDatabaseAbility ability
        private final String dbName

        DatabaseStateAssertion(RunOperationOnDatabaseAbility ability, String dbName) {
            this.ability = ability
            this.dbName = dbName
        }

        void hasRole(String roleName) {
            assert ability.databaseRoleExists(dbName, roleName) : "Expected role '${roleName}' to exist in database '${dbName}', but it doesn't."
        }

        void doesNotHaveRole(String roleName) {
            assert !ability.databaseRoleExists(dbName, roleName) : "Expected role '${roleName}' NOT to exist in database '${dbName}', but it does."
        }

        void doesNotHaveActiveSession(String roleName) {
            assert !ability.hasActiveSessions(dbName, roleName) :
                    "Expected no active sessions for role '${roleName}' in database '${dbName}', but some are still active."
        }

        void hasNoUnexpectedRoles() {
            assert ability.databaseHasNoUnexpectedRoles(dbName) : "Expected database '${dbName}' to have no unexpected roles, but found some."
        }

        void allowsConnectionFor(String username, String password) {
            ability.connectionToDatabaseSucceeds(dbName, username, password)
        }

        void rejectsConnectionFor(String username, String password, String expectedSqlState = "28P01") {
            ability.connectionToDatabaseShouldFailWithState(dbName, username, password, expectedSqlState)
        }
    }

    void updateShouldBeForbiddenFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ForbiddenOperationBuilder) Closure config) {
        performForbiddenOperation(config) { builder ->
            [sql: "UPDATE public.${builder.tableName} SET amount = ? WHERE id = ?", params: [100.0G, 1]]
        }
    }

    void insertShouldBeForbiddenFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ForbiddenOperationBuilder) Closure config) {
        performForbiddenOperation(config) { builder ->
            [sql: "INSERT INTO public.${builder.tableName} (amount) VALUES (?)", params: [999.0G]]
        }
    }

    void deleteShouldBeForbiddenFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ForbiddenOperationBuilder) Closure config) {
        performForbiddenOperation(config) { builder ->
            [sql: "DELETE FROM public.${builder.tableName} WHERE id = ?", params: [1]]
        }
    }

    void dropShouldBeForbiddenFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ForbiddenOperationBuilder) Closure config) {
        performForbiddenOperation(config, true) { builder ->
            [sql: "DROP TABLE public.${builder.tableName}", params: [] ]
        }
    }

    void updateShouldSucceedFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = UpdateOperationBuilder) Closure config) {
        performSuccessfulUpdate(config)
    }

    void insertShouldSucceedFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = InsertOperationBuilder) Closure config) {
        performSuccessfulInsert(config)
    }

    void deleteShouldSucceedFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = DeleteOperationBuilder) Closure config) {
        performSuccessfulDelete(config)
    }

    private void performForbiddenOperation(Closure config, boolean ownerCheck = false, Closure<Map<String, Object>> queryBuilder) {
        def builder = new ForbiddenOperationBuilder()
        config.delegate = builder; config.resolveStrategy = Closure.DELEGATE_FIRST; config.call()
        assert builder.tableName; assert builder.credentials
        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())
        def queryInfo = queryBuilder(builder)
        if (ownerCheck) {
            shouldFailWithOwnerError(builder.tableName) { jdbc.update(queryInfo.sql, queryInfo.params.toArray()) }
        } else {
            shouldFailWithPermissionError(builder.tableName) { jdbc.update(queryInfo.sql, queryInfo.params.toArray()) }
        }
    }

    private void performSuccessfulUpdate(Closure config) {
        def builder = new UpdateOperationBuilder()
        config.delegate = builder; config.resolveStrategy = Closure.DELEGATE_FIRST; config.call()
        assert builder.tableName; assert builder.credentials; assert builder.setData; assert builder.whereClause
        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())
        def setClauses = builder.setData.collect { k, v -> "${k} = ?" }.join(", ")
        def whereClauses = builder.whereClause.collect { k, v -> "${k} = ?" }.join(" AND ")
        def sql = "UPDATE public.${builder.tableName} SET ${setClauses} WHERE ${whereClauses}"
        def params = builder.setData.values() + builder.whereClause.values()
        try { jdbc.update(sql, params.toArray()) }
        catch (Exception e) { assert false: "Update failed: ${e.message}" }
    }

    private void performSuccessfulInsert(Closure config) {
        def builder = new InsertOperationBuilder()
        config.delegate = builder; config.resolveStrategy = Closure.DELEGATE_FIRST; config.call()
        assert builder.tableName; assert builder.credentials; assert builder.insertData
        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())
        def columns = builder.insertData.keySet().join(", ")
        def placeholders = builder.insertData.keySet().collect { "?" }.join(", ")
        def sql = "INSERT INTO public.${builder.tableName} (${columns}) VALUES (${placeholders})"
        def params = builder.insertData.values()
        try { jdbc.update(sql, params.toArray()) }
        catch (Exception e) { assert false: "Insert failed: ${e.message}" }
    }

    private void performSuccessfulDelete(Closure config) {
        def builder = new DeleteOperationBuilder()
        config.delegate = builder; config.resolveStrategy = Closure.DELEGATE_FIRST; config.call()
        assert builder.tableName; assert builder.credentials; assert builder.whereClause
        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())
        def whereClauses = builder.whereClause.collect { k, v -> "${k} = ?" }.join(" AND ")
        def sql = "DELETE FROM public.${builder.tableName} WHERE ${whereClauses}"
        def params = builder.whereClause.values()
        try { jdbc.update(sql, params.toArray()) }
        catch (Exception e) { assert false: "Delete failed: ${e.message}" }
    }

    private void shouldFailWithPermissionError(String tableName, Closure code) {
        try { code.call(); assert false: "Expected permission error!" }
        catch (Exception e) {
            Throwable cause = e.getCause() ?: e
            assert cause instanceof PSQLException : "Expected PSQLException, got ${cause?.class?.simpleName}"
            PSQLException error = (PSQLException) cause
            assert error.getSQLState() == "42501" : "Expected SQLState 42501, got ${error.getSQLState()}"
            assert error.message.contains("permission denied for table ${tableName}") || error.message.contains("permission denied for sequence") : "Unexpected error message: ${error.message}"
        }
    }

    private void shouldFailWithOwnerError(String tableName, Closure code) {
        try { code.call(); assert false: "Expected owner error!" }
        catch (Exception e) {
            Throwable cause = e.getCause() ?: e
            assert cause instanceof PSQLException : "Expected PSQLException, got ${cause?.class?.simpleName}"
            PSQLException error = (PSQLException) cause
            assert error.getSQLState() == "42501" : "Expected SQLState 42501, got ${error.getSQLState()}"
            assert error.message == "ERROR: must be owner of table ${tableName}" : "Unexpected error message: ${error.message}"
        }
    }

    String quoteIdentifier(JdbcTemplate jdbcTemplate, String identifier) {
        try {
            return jdbcTemplate.queryForObject("SELECT pg_catalog.quote_ident(?)", String.class, identifier)
        } catch (DataAccessException e) {
            throw new IllegalArgumentException("Invalid identifier for quoting: " + identifier, e)
        }
    }

    static class ForbiddenOperationBuilder {
        String tableName
        TemporaryAccessGrantedJson credentials

        void table(String tableName) { this.tableName = tableName }

        void table(Closure<String> tableNameClosure) { this.tableName = tableNameClosure.call() }

        void usingCredentials(TemporaryAccessGrantedJson credentials) { this.credentials = credentials }

        void usingCredentials(Closure<TemporaryAccessGrantedJson> credentialsClosure) {
            this.credentials = credentialsClosure.call()
        }
    }

    static class UpdateOperationBuilder {
        String tableName
        TemporaryAccessGrantedJson credentials
        Map<String, Object> setData
        Map<String, Object> whereClause

        void table(String tableName) { this.tableName = tableName }

        void table(Closure<String> tableNameClosure) { this.tableName = tableNameClosure.call() }

        void usingCredentials(TemporaryAccessGrantedJson credentials) { this.credentials = credentials }

        void usingCredentials(Closure<TemporaryAccessGrantedJson> credentialsClosure) {
            this.credentials = credentialsClosure.call()
        }

        void set(Map<String, Object> data) { this.setData = data }

        void where(Map<String, Object> condition) { this.whereClause = condition }
    }

    static class InsertOperationBuilder {
        String tableName
        TemporaryAccessGrantedJson credentials
        Map<String, Object> insertData

        void table(String tableName) { this.tableName = tableName }

        void table(Closure<String> tableNameClosure) { this.tableName = tableNameClosure.call() }

        void usingCredentials(TemporaryAccessGrantedJson credentials) { this.credentials = credentials }

        void usingCredentials(Closure<TemporaryAccessGrantedJson> credentialsClosure) {
            this.credentials = credentialsClosure.call()
        }

        void values(Map<String, Object> data) { this.insertData = data }
    }

    static class DeleteOperationBuilder {
        String tableName
        TemporaryAccessGrantedJson credentials
        Map<String, Object> whereClause

        void table(String tableName) { this.tableName = tableName }

        void table(Closure<String> tableNameClosure) { this.tableName = tableNameClosure.call() }

        void usingCredentials(TemporaryAccessGrantedJson credentials) { this.credentials = credentials }

        void usingCredentials(Closure<TemporaryAccessGrantedJson> credentialsClosure) {
            this.credentials = credentialsClosure.call()
        }

        void where(Map<String, Object> condition) { this.whereClause = condition }
    }
}
