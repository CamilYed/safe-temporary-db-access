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
            "UPDATE public.${builder.tableName} SET amount = 100 WHERE id = 1"
        }
    }

    void insertShouldBeForbiddenFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ForbiddenOperationBuilder) Closure config) {
        performForbiddenOperation(config) { builder ->
            "INSERT INTO public.${builder.tableName} (amount) VALUES (999)"
        }
    }

    void deleteShouldBeForbiddenFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ForbiddenOperationBuilder) Closure config) {
        performForbiddenOperation(config) { builder ->
            "DELETE FROM public.${builder.tableName} WHERE id = 1"
        }
    }

    void dropShouldBeForbiddenFor(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = ForbiddenOperationBuilder) Closure config) {
        performForbiddenOperation(config, true) { builder ->
            "DROP TABLE public.${builder.tableName}"
        }
    }

    void updateShouldSucceed(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())
        jdbc.update("UPDATE public.orders SET amount = 999.99 WHERE id = 1")
    }

    void insertShouldSucceed(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())
        jdbc.update("INSERT INTO public.orders (amount) VALUES (333.33)")
    }

    void deleteShouldSucceed(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())
        jdbc.update("DELETE FROM public.orders WHERE id = 1")
    }

    private void performForbiddenOperation(Closure config, boolean ownerCheck = false, Closure<String> queryBuilder) {
        def builder = new ForbiddenOperationBuilder()
        config.delegate = builder
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()

        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())

        if (ownerCheck) {
            shouldFailWithOwnerError(builder.tableName) {
                jdbc.execute(queryBuilder(builder))
            }
        } else {
            shouldFailWithPermissionError(builder.tableName) {
                jdbc.execute(queryBuilder(builder))
            }
        }
    }

    private void shouldFailWithPermissionError(String tableName, Closure code) {
        try {
            code.call()
            assert false: "Expected permission error but operation succeeded!"
        } catch (Exception e) {
            assert e.getCause() instanceof PSQLException : "Expected cause to be PSQLException but was ${e.getCause()?.class?.simpleName}"
            PSQLException error = (PSQLException) e.getCause()
            assert error.getSQLState() == "42501" : "Expected SQLState 42501 for permission denied"
            assert error.message == "ERROR: permission denied for table ${tableName}" : "Expected permission denied for table ${tableName}, but got: '${error.message}'"
        }
    }

    private void shouldFailWithOwnerError(String tableName, Closure code) {
        try {
            code.call()
            assert false: "Expected owner error but operation succeeded!"
        } catch (Exception e) {
            assert e.getCause() instanceof PSQLException : "Expected cause to be PSQLException but was ${e.getCause()?.class?.simpleName}"
            PSQLException error = (PSQLException) e.getCause()
            assert error.getSQLState() == "42501" : "Expected SQLState 42501 for owner error"
            assert error.message == "ERROR: must be owner of table ${tableName}" : "Expected must be owner of table ${tableName}, but got: '${error.message}'"
        }
    }

    static class ForbiddenOperationBuilder {
        String tableName
        TemporaryAccessGrantedJson credentials

        void table(String tableName) {
            this.tableName = tableName
        }

        void usingCredentials(TemporaryAccessGrantedJson credentials) {
            this.credentials = credentials
        }
    }
}
