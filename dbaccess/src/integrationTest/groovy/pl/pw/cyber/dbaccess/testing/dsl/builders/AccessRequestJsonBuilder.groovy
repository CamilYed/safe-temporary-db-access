package pl.pw.cyber.dbaccess.testing.dsl.builders

import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

@Builder(builderStrategy = SimpleStrategy, prefix = "with")
class AccessRequestJsonBuilder {
    String permissionLevel = "READ_ONLY"
    int durationMinutes = 60
    String targetDatabase = "test_db"

    static AccessRequestJsonBuilder anAccessRequest() {
        return new AccessRequestJsonBuilder()
    }

    Map toMap() {
        return [
                permissionLevel: permissionLevel,
                durationMinutes: durationMinutes,
                targetDatabase : targetDatabase
        ]
    }
}
