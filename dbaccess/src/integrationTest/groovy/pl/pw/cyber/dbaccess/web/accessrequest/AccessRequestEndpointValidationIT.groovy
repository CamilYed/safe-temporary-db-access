package pl.pw.cyber.dbaccess.web.accessrequest

import pl.pw.cyber.dbaccess.testing.BaseIT
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AccessRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.AddExampleUserAbility
import pl.pw.cyber.dbaccess.testing.dsl.abilities.DatabaseSetupAbility

import static pl.pw.cyber.dbaccess.testing.dsl.assertions.ResponseAssertion.assertThat
import static pl.pw.cyber.dbaccess.testing.dsl.builders.AccessRequestJsonBuilder.anAccessRequest
import static pl.pw.cyber.dbaccess.testing.dsl.builders.ResolvedDatabaseBuilder.aResolvableDatabase

class AccessRequestEndpointValidationIT extends BaseIT implements
        AccessRequestAbility,
        AddExampleUserAbility,
        DatabaseSetupAbility {

    def setup() {
        thereIsUser("user")
        thereIs(aResolvableDatabase())
    }

    def "should return 400 for missing permissionLevel"() {
        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withDurationMinutes(10)
                            .withPermissionLevel(null)
            )

        then:
            assertThat(response)
                    .hasStatusCode(400)
                    .hasValidationError("permissionLevel is required")
    }

    def "should return 400 for invalid permissionLevel"() {
        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withPermissionLevel("ADMIN")
                            .withDurationMinutes(10)
                            .withTargetDatabase("test1")
            )

        then:
            assertThat(response)
                    .hasStatusCode(400)
                    .hasValidationError("Invalid permissionLevel. Must be one of READ_ONLY, READ_WRITE, DELETE.")
    }

    def "should return 400 for invalid durationMinutes (too low)"() {
        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withPermissionLevel("READ_ONLY")
                            .withDurationMinutes(0)
                            .withTargetDatabase("test1")
            )

        then:
            assertThat(response)
                    .hasStatusCode(400)
                    .hasValidationError("durationMinutes must be between 1 and 60 minutes.")
    }

    def "should return 400 for invalid targetDatabase"() {
        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withTargetDatabase("nonexistent")
            )

        then:
            assertThat(response)
                    .hasStatusCode(400)
                    .hasValidationError("targetDatabase does not exist or is not properly configured.")
    }

    def "should return 400 for blank targetDatabase"() {
        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withPermissionLevel("READ_ONLY")
                            .withDurationMinutes(10)
                            .withTargetDatabase(" ")
            )

        then:
            assertThat(response)
                    .hasStatusCode(400)
                    .hasValidationError("targetDatabase must not be blank")
    }

    def "should return 400 with multiple validation errors"() {
        when:
            def response = accessRequest(
                    anAccessRequest()
                            .withPermissionLevel(null)
                            .withTargetDatabase(null)
                            .withDurationMinutes(0)
            )

        then:
            assertThat(response)
                    .hasStatusCode(400)
                    .hasValidationErrors([
                            "permissionLevel is required",
                            "targetDatabase is required",
                            "durationMinutes must be between 1 and 60 minutes."
                    ])
    }
}
