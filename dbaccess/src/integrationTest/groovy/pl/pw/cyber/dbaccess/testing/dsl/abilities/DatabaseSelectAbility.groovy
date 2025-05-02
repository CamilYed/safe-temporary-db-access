package pl.pw.cyber.dbaccess.testing.dsl.abilities


import pl.pw.cyber.dbaccess.web.accessrequest.TemporaryAccessGrantedJson

trait DatabaseSelectAbility extends DatabaseConnectionAbility {

    List<Map<String, Object>> selectFromOrders(TemporaryAccessGrantedJson credentials) {
        def db = databaseFor(credentials.targetDatabase())
        def jdbc = connect(credentials.username(), credentials.password(), db.url())
        return jdbc.queryForList("SELECT * FROM public.orders")
    }
}
