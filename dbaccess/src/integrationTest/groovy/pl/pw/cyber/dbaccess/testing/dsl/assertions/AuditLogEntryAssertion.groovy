package pl.pw.cyber.dbaccess.testing.dsl.assertions

import pl.pw.cyber.dbaccess.application.results.TemporaryAccessGranted
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLog
import pl.pw.cyber.dbaccess.testing.dsl.builders.MovableClock

import java.time.Instant
import java.time.format.DateTimeParseException

class AuditLogEntryAssertion {

    final TemporaryAccessAuditLog entry
    final MovableClock clock

    AuditLogEntryAssertion(TemporaryAccessAuditLog entry, MovableClock clock) {
        this.entry = entry
        this.clock = clock
    }

    AuditLogEntryAssertion hasRequestedBy(String u) { assert entry.requestedByUsername() == u; return this }

    AuditLogEntryAssertion hasGrantedForUser(String u) { assert entry.grantedUsername() == u; return this }

    AuditLogEntryAssertion hasGrantedForDatabase(String u) { assert entry.targetDatabase() == u; return this }

    AuditLogEntryAssertion hasGrantedForUser(TemporaryAccessGranted c) {
        hasGrantedForUser(c.username()); hasExpiresAt(c.expiresAt()); return this
    }

    AuditLogEntryAssertion hasDatabase(String d) { assert entry.targetDatabase() == d; return this }

    AuditLogEntryAssertion hasPermission(String p) { assert entry.permissionLevel() == p; return this }

    AuditLogEntryAssertion hasGrantedAt(String isoTimestamp) {
        try {
            Instant expectedInstant = Instant.parse(isoTimestamp)
            return hasGrantedAt(expectedInstant)
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid ISO-8601 format for hasGrantedAt: '${isoTimestamp}'", e)
        }
    }

    AuditLogEntryAssertion hasGrantedAt(Instant t) {
        assert entry.grantedAt() == t: "Expected grantedAt ${t}, but was ${entry.grantedAt()}"; return this
    }

    AuditLogEntryAssertion hasExpiresAt(String isoTimestamp) {
        try {
            Instant expectedInstant = Instant.parse(isoTimestamp)
            return hasExpiresAt(expectedInstant)
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Invalid ISO-8601 format for hasExpiresAt: '${isoTimestamp}'", e)
        }
    }

    AuditLogEntryAssertion hasExpiresAt(Instant t) {
        assert entry.expiresAt() == t: "Expected expiresAt ${t}, but was ${entry.expiresAt()}"; return this
    }

    AuditLogEntryAssertion hasNotRevokedStatus() { assert !entry.revoked(); return this }

    AuditLogEntryAssertion hasRevokedStatus() { assert entry.revoked(); return this }

}
