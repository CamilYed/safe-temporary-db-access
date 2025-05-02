package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.testing.dsl.builders.MovableClock

import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

trait ClockControlAbility {

    @Autowired
    private MovableClock testClock

    void currentTimeIs(String isoInstant) {
        try {
            testClock.setInstant(Instant.parse(isoInstant))
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Expected ISO-8601 instant, e.g. 2025-04-07T12:00:00Z", e)
        }
    }

    void timeElapsed(Duration duration) {
        testClock.moveForward(duration)
    }

    void timeWentBack(Duration duration) {
        testClock.moveBackward(duration)
    }

    Instant currentTime() {
        return testClock.instant()
    }

    MovableClock testClock() {
        return testClock
    }
}