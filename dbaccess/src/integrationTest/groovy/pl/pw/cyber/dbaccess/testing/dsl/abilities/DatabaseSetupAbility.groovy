package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.domain.ResolvedDatabase
import pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder
import pl.pw.cyber.dbaccess.testing.dsl.fakes.FakeDatabaseConfigurationProvider

trait DatabaseSetupAbility {

    @Autowired
    private FakeDatabaseConfigurationProvider databaseConfigurationProvider

    void thereIs(ResolvedDatabaseBuilder builder) {
        ResolvedDatabase db = builder.build()
        databaseConfigurationProvider.add(db)
    }
}