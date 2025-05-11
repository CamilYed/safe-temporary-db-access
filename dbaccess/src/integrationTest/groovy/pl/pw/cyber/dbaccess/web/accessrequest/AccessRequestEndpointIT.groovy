package pl.pw.cyber.dbaccess.web.accessrequest

import ch.qos.logback.classic.Level
import pl.pw.cyber.dbaccess.testing.MongoBaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddAuditLogAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.DatabaseSelectAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.ExtractAccessResponseAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.LogCaptureAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.MetricAssertionAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.MongoAuditAssertionAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.RunOperationOnDatabaseAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.SchedulingControlAbility
import pl.pw.cyber.dbaccess.testing.dsl.assertions.DatabaseResultAssertion

import java.time.Duration

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder.anAccessRequest
import static pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder.aResolvableDatabase
import static pl.pw.cyber.dbaccess.testing.dsl.builders.TemporaryAccessAuditLogBuilder.anExpiredAuditLog
import static pl.pw.cyber.dbaccess.testing.dsl.builders.TemporaryAccessAuditLogBuilder.anExpiredInvalidAuditLog

class AccessRequestEndpointIT extends MongoBaseIT implements
        AccessRequestAbility,
        AddExampleUserAbility,
        RunOperationOnDatabaseAbility,
        DatabaseResultAssertion,
        ExtractAccessResponseAbility,
        DatabaseSelectAbility,
        MongoAuditAssertionAbility,
        SchedulingControlAbility,
        AddAuditLogAbility,
        LogCaptureAbility,
        MetricAssertionAbility {

    def setup() {
        thereIsUser("user")
        setupLogCapture("pl.pw.cyber.dbaccess.application.TemporaryDbAccessService")
    }

    def cleanup() {
        stopDatabases()
    }

    def "should not create temporary user when database is resolvable but not running"() {
        given:
            thereIs(aResolvableDatabase().databaseName("test_db"))

        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withTargetDatabase("test_db")
                            .withPermissionLevel("READ_ONLY")
                            .withDurationMinutes(10)

            )

        then:
            assertThat(response).hasStatusCode(500)
        and:
            metricWasExposed {
                hasName("access_failed_total")
                hasTag("database", "test_db")
                hasTag("permission", "READ_ONLY")
                hasTag("ttl", "10")
                hasValueGreaterThan(0.0)
            }
    }

    def "should create READ_ONLY user with correct privileges in PostgreSQL"() {
        given:
            resolvedDatabaseIsRunning(aResolvableDatabase().databaseName("test_db"))
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
                usingCredentials credentials
            }
        and:
            insertShouldBeForbiddenFor {
                table "orders"
                usingCredentials credentials
            }
        and:
            deleteShouldBeForbiddenFor {
                table "orders"
                usingCredentials credentials
            }
        and:
            dropShouldBeForbiddenFor {
                table "orders"
                usingCredentials credentials
            }
        and:
            metricWasExposed {
                hasName("access_success_total")
                hasTag("database", "test_db")
                hasTag("permission", "READ_ONLY")
                hasValueGreaterThan(0.0)
            }
    }

    def "should create READ_WRITE user with correct privileges in PostgreSQL"() {
        given:
            resolvedDatabaseIsRunning(aResolvableDatabase().databaseName("test_db"))
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
            def initialRows = selectFromOrders(credentials)
            assertThatRows(initialRows)
                    .hasNumberOfRows(2)
                    .hasRowWithId(1) { hasAmount(100.50G) }
                    .hasRowWithId(2) { hasAmount(200.75G) }
        and:
            updateShouldSucceedFor {
                table "orders"
                usingCredentials credentials
                set amount: 999.99G
                where id: 1
            }
        and:
            insertShouldSucceedFor {
                table "orders"
                usingCredentials credentials
                values amount: 333.33G
            }
        then:
            def modifiedRows = selectFromOrders(credentials)
            assertThatRows(modifiedRows)
                    .hasNumberOfRows(3)
                    .hasRowWithId(1) { hasAmount(999.99G) }
                    .hasRowWithId(2) { hasAmount(200.75G) }
                    .hasRowWithId(3) { hasAmount(333.33G) }
        and:
            deleteShouldBeForbiddenFor {
                table "orders"
                usingCredentials credentials
            }
        and:
            dropShouldBeForbiddenFor {
                table "orders"
                usingCredentials credentials
            }
        and:
            metricWasExposed {
                hasName("access_success_total")
                hasTag("database", "test_db")
                hasTag("permission", "READ_WRITE")
                hasValueGreaterThan(0.0)
            }
    }

    def "should create DELETE user with correct privileges in PostgreSQL"() {
        given:
            resolvedDatabaseIsRunning(aResolvableDatabase().databaseName("test_db"))
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
            def initialRows = selectFromOrders(credentials)
            assertThatRows(initialRows)
                    .hasNumberOfRows(2)
                    .hasRowWithId(1) { hasAmount(100.50G) }
                    .hasRowWithId(2) { hasAmount(200.75G) }
        and:
            deleteShouldSucceedFor {
                table "orders"
                usingCredentials credentials
                where id: 1
            }
        then:
            def rowsAfterDelete = selectFromOrders(credentials)
            assertThatRows(rowsAfterDelete)
                    .hasNumberOfRows(1)
                    .hasRowWithId(2) { hasAmount(200.75G) }
        and:
            updateShouldBeForbiddenFor {
                table "orders"
                usingCredentials credentials
            }
        and:
            insertShouldBeForbiddenFor {
                table "orders"
                usingCredentials credentials
            }
        and:
            dropShouldBeForbiddenFor {
                table "orders"
                usingCredentials credentials
            }
        and:
            metricWasExposed {
                hasName("access_success_total")
                hasTag("database", "test_db")
                hasTag("permission", "DELETE")
                hasValueGreaterThan(0.0)
            }
    }

    def "should do nothing when there are no expired access logs"() {
        given:
            currentTimeIs("2025-05-04T12:00:00Z")
            theAuditLog { shouldBeEmpty() }

        when:
            manuallyTriggerScheduler()

        then:
            theAuditLog { shouldBeEmpty() }

        and:
            metricWasNotExposed {
                hasName("revoke_success_total")
            }
    }

    def "should handle failure when no resolving database"() {
        given:
            thereIs(anExpiredInvalidAuditLog().withTargetDatabase("nonexistent_db"))

        and:
            noAnyResolvableDatabases()

        when:
            manuallyTriggerScheduler()

        then:
            noExceptionThrown()
        and:
            metricWasExposed {
                hasName("revoke_failed_total")
                hasTag("database", "nonexistent_db")
                hasValueGreaterThan(0.0)
            }
    }

    def "should handle failure when revoking invalid audit log"() {
        given:
            thereIs(anExpiredInvalidAuditLog().withTargetDatabase("test_db").withGrantedUsername("nonexistent_user"))

        and:
            resolvedDatabaseIsRunning(aResolvableDatabase().databaseName("test_db"))

        when:
            manuallyTriggerScheduler()

        then:
            noExceptionThrown()

        and:
            metricWasExposed {
                hasName("revoke_failed_total")
                hasTag("database", "test_db")
                hasValueGreaterThan(0.0)
            }
    }

    def "should handle failure when revoking due to postgres not available"() {
        given:
            thereIs(anExpiredAuditLog().withTargetDatabase("test_db"))

        when:
            manuallyTriggerScheduler()

        then:
            noExceptionThrown()

        and:
            metricWasExposed {
                hasName("revoke_failed_total")
                hasTag("database", "test_db")
                hasValueGreaterThan(0.0)
            }
    }

    def "should create audit log entry upon successful access request"() {
        given:
            resolvedDatabaseIsRunning(aResolvableDatabase().databaseName("audit_test_db"))
        and:
            theAuditLog { shouldBeEmpty() }
        and:
            currentTimeIs("2025-05-02T12:00:00Z")

        when:
            def response = accessRequestBy("user") {
                anAccessRequest()
                        .withTargetDatabase("audit_test_db")
                        .withPermissionLevel("READ_ONLY")
                        .withDurationMinutes(60)
            }
        then:
            assertThat(response).isOK()
            def credentials = extractFromResponse(response)

        and:
            theAuditLog {
                shouldHaveSingleEntry {
                    hasRequestedBy("user")
                    hasGrantedForUser(credentials.username())
                    hasGrantedForDatabase("audit_test_db")
                    hasPermission("READ_ONLY")
                    hasGrantedAt("2025-05-02T12:00:00Z")
                    hasExpiresAt("2025-05-02T13:00:00Z")
                    hasNotRevokedStatus()
                }
            }
    }

    def "should revoke expired user access via scheduler"() {
        given:
            currentTimeIs("2025-05-03T14:00:00Z")
        and:
            resolvedDatabaseIsRunning(aResolvableDatabase().databaseName("revoke_db"))
        and:
            publicSchemaOfDatabaseHasTable("revoke_db") {
                table("orders") {
                    withColumn "id SERIAL PRIMARY KEY"
                    withColumn "amount DECIMAL(10,2)"
                    withRow amount: 100.50
                    withRow amount: 200.75
                }
            }

        when:
            def response = accessRequestBy("user") {
                anAccessRequest()
                        .withTargetDatabase("revoke_db")
                        .withPermissionLevel("READ_ONLY")
                        .withDurationMinutes(1)
            }

        then:
            assertThat(response).isOK()

        and:
            def credentials = extractFromResponse(response)

        and:
            def initialRows = selectFromOrders(credentials)
            assertThatRows(initialRows)
                    .hasNumberOfRows(2)
                    .hasRowWithId(1) { hasAmount(100.50G) }
                    .hasRowWithId(2) { hasAmount(200.75G) }

        and:
            def grantedUsername = credentials.username()
            database("revoke_db") {
                hasRole(grantedUsername)
                allowsConnectionFor(grantedUsername, credentials.password())
            }

        and:
            theAuditLog {
                shouldHaveSingleEntry {
                    hasRequestedBy("user")
                    hasTargetDatabase("revoke_db")
                    hasGrantedForUser(grantedUsername)
                    hasGrantedAt("2025-05-03T14:00:00Z")
                    hasExpiresAt("2025-05-03T14:01:00Z")
                    hasNotRevokedStatus()
                }
            }

        when:
            timeElapsed(Duration.ofMinutes(1).plusSeconds(1))

        and:
            manuallyTriggerScheduler()

        then:
            eventually {
                database("revoke_db") {
                    doesNotHaveRole(grantedUsername)
                    doesNotHaveActiveSession(grantedUsername)
                }
                theAuditLog {
                    shouldHaveSingleEntry {
                        hasRevokedStatus()
                    }
                }
            }
        and:
            metricWasExposed {
                hasName("revoke_success_total")
                hasTag("database", "revoke_db")
                hasValueGreaterThan(0.0)
            }
    }

    def "should revoke user when no roles are assigned"() {
        given:
            resolvedDatabaseIsRunning(
                    aResolvableDatabase()
                            .databaseName("roleless_db")
                            .databaseUserName("adming5m9dzq")
            )

        and:
            thereIs(anExpiredInvalidAuditLog()
                    .withTargetDatabase("roleless_db")
                    .withGrantedUsername("user7g5m9dzq")
            )

        and:
            thereIsUserInDatabase("roleless_db") {
                withUsername("user7g5m9dzq")
                withPassword("irrelevant")
            }

        and:
            currentUserOfDb("roleless_db") {
                hasName("adming5m9dzq")
            }

        when:
            manuallyTriggerScheduler()

        then:
            noExceptionThrown()

        and:
            theAuditLog {
                shouldHaveSingleEntry {
                    hasGrantedForUser("user7g5m9dzq")
                    hasTargetDatabase("roleless_db")
                    hasRevokedStatus()
                }
            }

        and:
            metricWasExposed {
                hasName("revoke_success_total")
                hasTag("database", "roleless_db")
                hasValueGreaterThan(0.0)
            }
    }

    def "should log exception for unsafe username"() {
        given:
            def invalidUsername = "user; DROP DATABASE prod;"
            def dbName = "safe_db"

        and:
            resolvedDatabaseIsRunning(aResolvableDatabase()
                    .databaseName(dbName)
                    .databaseUserName("adming5m9dzq")
            )

        and:
            thereIsUserInDatabase(dbName) {
                withUsername(invalidUsername)
            }

            thereIs(anExpiredAuditLog().withTargetDatabase(dbName).withGrantedUsername(invalidUsername))

        when:
            manuallyTriggerScheduler()

        then:
            logCaptured("Unsafe identifier", Level.ERROR)
    }

    def "should log for unsafe role name during revoke"() {
        given:
            def dbName = "unsafe_role_db"
            resolvedDatabaseIsRunning(
                    aResolvableDatabase()
                            .databaseName(dbName)
                            .databaseUserName("adming5m9dzq")
            )

        and:
            thereIsUserInDatabase(dbName) {
                2
                withUsername("bad;role")
            }

        and:
            thereIs(anExpiredAuditLog().withTargetDatabase(dbName).withGrantedUsername("bad;role"))

        when:
            manuallyTriggerScheduler()

        then:
            logCaptured("Unsafe identifier", Level.ERROR)
    }
}