package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import pl.pw.cyber.dbaccess.web.accessrequest.TemporaryAccessGrantedJson

trait DatabaseConnectionAbility extends DatabaseSetupAbility {

    void connectionToDatabaseSucceeds(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())

        jdbc.queryForObject("SELECT 1", Integer)
    }

    void updateOperationIsNotAvailable(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())

        shouldFail {
            jdbc.update("UPDATE orders SET amount = 100 WHERE id = 1")
        }
    }

    void insertOperationIsNotAvailable(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())

        shouldFail {
            jdbc.update("INSERT INTO orders (id, amount) VALUES (999, 100)")
        }
    }

    void deleteOperationIsNotAvailable(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())

        shouldFail {
            jdbc.update("DELETE FROM orders WHERE id = 1")
        }
    }

    void dropOperationIsNotAvailable(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())

        shouldFail {
            jdbc.execute("DROP TABLE orders")
        }
    }

    JdbcTemplate connect(String username, String password, String jdbcUrl) {
        def ds = new DriverManagerDataSource(jdbcUrl, username, password)
        return new JdbcTemplate(ds)
    }

    private void shouldFail(Closure code) {
        try {
            code.call()
            assert false : "Expected exception but code succeeded!"
        } catch (Exception ignored) {
            // ok
        }
    }
}
