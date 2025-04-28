//package pl.pw.cyber.dbaccess.testing.dsl.abilities
//
//import org.postgresql.util.PSQLException
//import pl.pw.cyber.dbaccess.web.accessrequest.TemporaryAccessGrantedJson
//
//trait RunOperationOnDatabaseAbility extends DatabaseConnectionAbility {
//
//    void connectionToDatabaseSucceeds(TemporaryAccessGrantedJson credentials) {
//        def db = databaseFor(credentials.targetDatabase())
//        def jdbc = connect(credentials.username(), credentials.password(), db.url())
//
//        jdbc.queryForObject("SELECT 1", Integer)
//    }
//
//    void updateOperationIsNotAvailable(TemporaryAccessGrantedJson credentials) {
//        def db = databaseFor(credentials.targetDatabase())
//        def jdbc = connect(credentials.username(), credentials.password(), db.url())
//
//        shouldFailWithPermissionError {
//            jdbc.update("UPDATE public.orders SET amount = 100 WHERE id = 1")
//        }
//    }
//
//    void insertOperationIsNotAvailable(TemporaryAccessGrantedJson credentials) {
//        def db = databaseFor(credentials.targetDatabase())
//        def jdbc = connect(credentials.username(), credentials.password(), db.url())
//
//        shouldFailWithPermissionError {
//            jdbc.update("INSERT INTO public.orders (id, amount) VALUES (999, 100)")
//        }
//    }
//
//    void deleteOperationIsNotAvailable(TemporaryAccessGrantedJson credentials) {
//        def db = databaseFor(credentials.targetDatabase())
//        def jdbc = connect(credentials.username(), credentials.password(), db.url())
//
//        shouldFailWithPermissionError {
//            jdbc.update("DELETE FROM public.orders WHERE id = 1")
//        }
//    }
//
//    void dropOperationIsNotAvailable(TemporaryAccessGrantedJson credentials) {
//        def db = databaseFor(credentials.targetDatabase())
//        def jdbc = connect(credentials.username(), credentials.password(), db.url())
//
//        shouldFailWithOwnerError {
//            jdbc.execute("DROP TABLE public.orders")
//        }
//    }
//
//    private void shouldFailWithPermissionError(Closure code) {
//        try {
//            code.call()
//            assert false: "Expected permission exception but code succeeded!"
//        } catch (Exception e) {
//            assert e.getCause() != null: "Expected cause is not null"
//            assert e.getCause() instanceof PSQLException
//            PSQLException error = (PSQLException) e.getCause()
//            assert error.getSQLState() == "42501" : "Expected 42501 sql error code"
//            assert error.message == "ERROR: permission denied for table orders"
//        }
//    }
//
//    private void shouldFailWithOwnerError(Closure code) {
//        try {
//            code.call()
//            assert false: "Expected permission exception but code succeeded!"
//        } catch (Exception e) {
//            assert e.getCause() != null: "Expected cause is not null"
//            assert e.getCause() instanceof PSQLException
//            PSQLException error = (PSQLException) e.getCause()
//            assert error.getSQLState() == "42501" : "Expected 42501 sql error code"
//            assert error.message == "ERROR: must be owner of table orders"
//        }
//    }
//}
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

    private void performForbiddenOperation(Closure config, boolean ownerCheck = false, Closure<String> queryBuilder) {
        def builder = new ForbiddenOperationBuilder()
        config.delegate = builder
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()

        def db = databaseFor(builder.credentials.targetDatabase())
        def jdbc = connect(builder.credentials.username(), builder.credentials.password(), db.url())

        if (ownerCheck) {
            shouldFailWithOwnerError {
                jdbc.execute(queryBuilder(builder))
            }
        } else {
            shouldFailWithPermissionError {
                jdbc.execute(queryBuilder(builder))
            }
        }
    }

    private void shouldFailWithPermissionError(Closure code) {
        try {
            code.call()
            assert false: "Expected permission error but operation succeeded!"
        } catch (Exception e) {
            assert e.getCause() instanceof PSQLException
            PSQLException error = (PSQLException) e.getCause()
            assert error.getSQLState() == "42501" : "Expected SQLState 42501 for permission denied"
            assert error.message.contains("permission denied") : "Expected permission denied error, but got: ${error.message}"
        }
    }

    private void shouldFailWithOwnerError(Closure code) {
        try {
            code.call()
            assert false: "Expected owner error but operation succeeded!"
        } catch (Exception e) {
            assert e.getCause() instanceof PSQLException
            PSQLException error = (PSQLException) e.getCause()
            assert error.getSQLState() == "42501" : "Expected SQLState 42501 for owner error"
            assert error.message.contains("must be owner") : "Expected must be owner error, but got: ${error.message}"
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
