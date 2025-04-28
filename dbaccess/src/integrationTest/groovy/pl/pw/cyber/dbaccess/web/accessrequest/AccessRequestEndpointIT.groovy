package pl.pw.cyber.dbaccess.web.accessrequest

import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.DatabaseConnectionAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.DatabaseSetupAbility
import pl.pw.cyber.dbaccess.testing.dsl.assertions.DatabaseResultAssertion

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder.anAccessRequest
import static pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder.aResolvableDatabase

class AccessRequestEndpointIT extends BaseIT implements
        AccessRequestAbility,
        AddExampleUserAbility,
        DatabaseSetupAbility,
        DatabaseConnectionAbility,
        DatabaseResultAssertion {

    def setup() {
        thereIsUser("user")
    }

    def cleanup() {
        stopDatabase()
    }

    def "should create READ_ONLY user with correct privileges in PostgreSQL"() {
        given: "Running database with a sample 'orders' table"
            resolvedDatabaseIsRunning(aResolvableDatabase().withName("test_db"))

        and:
            publicSchemaOfDatabaseHasTable("test_db") {
                table("orders") {
                    withColumn "id SERIAL PRIMARY KEY"
                    withColumn "amount DECIMAL(10,2)"
                    withRow amount: 100.50
                    withRow amount: 200.75
                }
            }

        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withPermissionLevel("READ_ONLY")
                            .withDurationMinutes(20)
                            .withTargetDatabase("test_db")
            )

        then:
            assertThat(response).isOK()

        and:
            var credentials = extractFromResponse(response)
            connectionToDatabaseSucceeds(credentials)

        and: "Can SELECT rows from 'orders' table"
            def rows = selectFromOrders(credentials)
            assertThatRows(rows)
                    .hasNumberOfRows(2)
                    .hasRowWithId(1) { hasAmount(100.50G) }
                    .hasRowWithId(2) { hasAmount(200.75G) }

        and: "Cannot perform forbidden operations (INSERT, UPDATE, DELETE, DROP)"
            updateOperationIsNotAvailable(credentials)
            insertOperationIsNotAvailable(credentials)
            deleteOperationIsNotAvailable(credentials)
            dropOperationIsNotAvailable(credentials)
    }
}