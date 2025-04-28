package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DriverManagerDataSource
import pl.pw.cyber.dbaccess.web.accessrequest.TemporaryAccessGrantedJson

trait DatabaseSelectAbility extends DatabaseSetupAbility {

    List<Map<String, Object>> selectFromOrders(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())
        return jdbc.queryForList("SELECT * FROM orders")
    }

    private JdbcTemplate connect(String username, String password, String jdbcUrl) {
        def ds = new DriverManagerDataSource(jdbcUrl, username, password)
        return new JdbcTemplate(ds)
    }
}
