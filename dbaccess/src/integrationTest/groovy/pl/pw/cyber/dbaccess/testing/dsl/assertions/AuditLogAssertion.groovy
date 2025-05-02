package pl.pw.cyber.dbaccess.testing.dsl.assertions

import pl.pw.cyber.dbaccess.adapters.mongo.MongoTemplateAuditLogTestFetcher
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog
import pl.pw.cyber.dbaccess.testing.dsl.builders.MovableClock

class AuditLogAssertion {
    final MongoTemplateAuditLogTestFetcher repository
    final MovableClock clock

    AuditLogAssertion(MongoTemplateAuditLogTestFetcher repository, MovableClock clock) {
        this.repository = repository
        this.clock = clock
    }

    void shouldHaveSingleEntry(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AuditLogEntryAssertion) Closure entryAssertions) {
        List<TemporaryAccessAuditLog> allLogs = repository.findAllDocuments()
        assert allLogs.size() == 1 : "Expected exactly one audit log entry, but found ${allLogs.size()}"
        def entry = allLogs[0]
        def entryAssertion = new AuditLogEntryAssertion(entry, clock)
        entryAssertions.delegate = entryAssertion
        entryAssertions.resolveStrategy = Closure.DELEGATE_FIRST
        entryAssertions.call()
    }

    void shouldHaveNumberOfEntries(int expectedCount) {
        def actualCount = repository.countDocuments()
        assert actualCount == expectedCount : "Expected ${expectedCount} audit log entries, but found ${actualCount}"
    }

    void shouldBeEmpty() { shouldHaveNumberOfEntries(0) }
}
