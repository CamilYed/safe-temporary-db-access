package pl.pw.cyber.dbaccess.web.accessrequest

import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.DatabaseSetupAbility
import spock.lang.Ignore

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder.anAccessRequest
import static pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder.aResolvableDatabase

class AccessRequestEndpointIT extends BaseIT implements
        AccessRequestAbility,
        AddExampleUserAbility,
        DatabaseSetupAbility {

    def setup() {
        thereIsUser("user")
        thereIs(aResolvableDatabase().withName("test_db"))
    }

    @Ignore
    def "should create READ_ONLY user with correct privileges in PostgreSQL"() {
        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withPermissionLevel("READ_ONLY")
                            .withDurationMinutes(20)
                            .withTargetDatabase("test_db")
            )

        then:
            assertThat(response).isOK()

    }
}