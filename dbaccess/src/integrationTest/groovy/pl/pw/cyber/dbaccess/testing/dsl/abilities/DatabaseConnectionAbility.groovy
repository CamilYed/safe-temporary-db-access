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

    JdbcTemplate connect(String username, String password, String jdbcUrl) {
        def ds = new DriverManagerDataSource(jdbcUrl, username, password)
        return new JdbcTemplate(ds)
    }
}
