package pl.pw.cyber.dbaccess.web.accessrequest

import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.DatabaseSelectAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.ExtractAccessResponseAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.RunOperationOnDatabaseAbility
import pl.pw.cyber.dbaccess.testing.dsl.assertions.DatabaseResultAssertion

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder.anAccessRequest
import static pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder.aResolvableDatabase

class AccessRequestEndpointIT extends BaseIT implements
        AccessRequestAbility,
        AddExampleUserAbility,
        RunOperationOnDatabaseAbility,
        DatabaseResultAssertion,
        ExtractAccessResponseAbility,
        DatabaseSelectAbility {

    def setup() {
        thereIsUser("user")
    }

    def cleanup() {
        stopDatabase()
    }

    def "should create READ_ONLY user with correct privileges in PostgreSQL"() {
        given:
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
                            .withTargetDatabase("test_db")
                            .withPermissionLevel("READ_ONLY")
            )

        then:
            assertThat(response).isOK()

        and:
            var credentials = extractFromResponse(response)
            connectionToDatabaseSucceeds(credentials)

        and:
            def rows = selectFromOrders(credentials)
            assertThatRows(rows)
                    .hasNumberOfRows(2)
                    .hasRowWithId(1) { hasAmount(100.50G) }
                    .hasRowWithId(2) { hasAmount(200.75G) }

        and:
            updateShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }

        and:
            insertShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }

        and:
            deleteShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }

        and:
            dropShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }
    }

    def "should create READ_WRITE user with correct privileges in PostgreSQL"() {
        given:
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
                            .withTargetDatabase("test_db")
                            .withPermissionLevel("READ_WRITE")
            )

        then:
            assertThat(response).isOK()

        and:
            var credentials = extractFromResponse(response)
            connectionToDatabaseSucceeds(credentials)

        and:
            def rows = selectFromOrders(credentials)
            assertThatRows(rows)
                    .hasNumberOfRows(2)
                    .hasRowWithId(1) { hasAmount(100.50G) }
                    .hasRowWithId(2) { hasAmount(200.75G) }

        and: "Should allow INSERT and UPDATE but forbid DELETE and DROP"
            updateShouldSucceed(credentials)
            insertShouldSucceed(credentials)

            deleteShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }

            dropShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }
    }

    def "should create DELETE user with correct privileges in PostgreSQL"() {
        given:
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
                            .withTargetDatabase("test_db")
                            .withPermissionLevel("DELETE")
            )

        then:
            assertThat(response).isOK()

        and:
            var credentials = extractFromResponse(response)
            connectionToDatabaseSucceeds(credentials)

        and:
            def rows = selectFromOrders(credentials)
            assertThatRows(rows)
                    .hasNumberOfRows(2)
                    .hasRowWithId(1) { hasAmount(100.50G) }
                    .hasRowWithId(2) { hasAmount(200.75G) }

        and: "Should allow DELETE but forbid INSERT, UPDATE, and DROP"
            deleteShouldSucceed(credentials)

            updateShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }

            insertShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }

            dropShouldBeForbiddenFor {
                table "orders"
                usingCredentials(credentials)
            }
    }
}