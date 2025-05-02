package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.postgresql.util.PSQLException
import pl.pw.cyber.dbaccess.web.accessrequest.TemporaryAccessGrantedJson

trait RunOperationOnDatabaseAbility extends DatabaseConnectionAbility {

    void connectionToDatabaseSucceeds(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())
        jdbc.queryForObject("SELECT 1", Integer)
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
            [sql: "DROP TABLE public.${builder.tableName}", params: []]
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
        config.delegate = builder
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()

        assert builder.tableName: "Table name must be specified using 'table \"name\"' or 'table { \"name\" }'"
        assert builder.credentials: "Credentials must be specified using 'usingCredentials creds' or 'usingCredentials { creds }'"

        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())
        def queryInfo = queryBuilder(builder)

        if (ownerCheck) {
            shouldFailWithOwnerError(builder.tableName) {
                jdbc.update(queryInfo.sql, queryInfo.params.toArray())
            }
        } else {
            shouldFailWithPermissionError(builder.tableName) {
                jdbc.update(queryInfo.sql, queryInfo.params.toArray())
            }
        }
    }

    private void performSuccessfulUpdate(Closure config) {
        def builder = new UpdateOperationBuilder()
        config.delegate = builder
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()

        assert builder.tableName: "Table name must be specified using 'table \"name\"' or 'table { \"name\" }'"
        assert builder.credentials: "Credentials must be specified using 'usingCredentials creds' or 'usingCredentials { creds }'"
        assert builder.setData: "Data to set must be specified using 'set'"
        assert builder.whereClause: "Where clause must be specified using 'where'"

        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())

        def setClauses = builder.setData.collect { key, value -> "${key} = ?" }.join(", ")
        def whereClauses = builder.whereClause.collect { key, value -> "${key} = ?" }.join(" AND ")
        def sql = "UPDATE public.${builder.tableName} SET ${setClauses} WHERE ${whereClauses}"
        def params = builder.setData.values() + builder.whereClause.values()

        try {
            jdbc.update(sql, params.toArray())
        } catch (Exception e) {
            assert false: "Update operation failed unexpectedly for user ${builder.credentials.username()} on table ${builder.tableName}: ${e.message}"
        }
    }

    private void performSuccessfulInsert(Closure config) {
        def builder = new InsertOperationBuilder()
        config.delegate = builder
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()

        assert builder.tableName: "Table name must be specified using 'table \"name\"' or 'table { \"name\" }'"
        assert builder.credentials: "Credentials must be specified using 'usingCredentials creds' or 'usingCredentials { creds }'"
        assert builder.insertData: "Data to insert must be specified using 'values'"

        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())

        def columns = builder.insertData.keySet().join(", ")
        def placeholders = builder.insertData.keySet().collect { "?" }.join(", ")
        def sql = "INSERT INTO public.${builder.tableName} (${columns}) VALUES (${placeholders})"
        def params = builder.insertData.values()

        try {
            jdbc.update(sql, params.toArray())
        } catch (Exception e) {
            assert false: "Insert operation failed unexpectedly for user ${builder.credentials.username()} on table ${builder.tableName}: ${e.message}"
        }
    }

    private void performSuccessfulDelete(Closure config) {
        def builder = new DeleteOperationBuilder()
        config.delegate = builder
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()

        assert builder.tableName: "Table name must be specified using 'table \"name\"' or 'table { \"name\" }'"
        assert builder.credentials: "Credentials must be specified using 'usingCredentials creds' or 'usingCredentials { creds }'"
        assert builder.whereClause: "Where clause must be specified using 'where'"

        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())

        def whereClauses = builder.whereClause.collect { key, value -> "${key} = ?" }.join(" AND ")
        def sql = "DELETE FROM public.${builder.tableName} WHERE ${whereClauses}"
        def params = builder.whereClause.values()

        try {
            jdbc.update(sql, params.toArray())
        } catch (Exception e) {
            assert false: "Delete operation failed unexpectedly for user ${builder.credentials.username()} on table ${builder.tableName}: ${e.message}"
        }
    }

    private void shouldFailWithPermissionError(String tableName, Closure code) {
        try {
            code.call()
            assert false: "Expected permission error but operation succeeded!"
        } catch (Exception e) {
            Throwable cause = e.cause ?: e
            assert cause instanceof PSQLException: "Expected cause to be PSQLException but was ${cause?.class?.simpleName}"
            PSQLException error = (PSQLException) cause
            assert error.getSQLState() == "42501": "Expected SQLState 42501 for permission denied, but got ${error.getSQLState()}"
            assert error.message.contains("permission denied for table ${tableName}") || error.message.contains("permission denied for sequence"): "Expected permission denied for table ${tableName} or its sequence, but got: '${error.message}'"
        }
    }

    private void shouldFailWithOwnerError(String tableName, Closure code) {
        try {
            code.call()
            assert false: "Expected owner error but operation succeeded!"
        } catch (Exception e) {
            Throwable cause = e.cause ?: e
            assert cause instanceof PSQLException: "Expected cause to be PSQLException but was ${cause?.class?.simpleName}"
            PSQLException error = (PSQLException) cause
            assert error.getSQLState() == "42501": "Expected SQLState 42501 for owner error, but got ${error.getSQLState()}"
            assert error.message == "ERROR: must be owner of table ${tableName}": "Expected must be owner of table ${tableName}, but got: '${error.message}'"
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
