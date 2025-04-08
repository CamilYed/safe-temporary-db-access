package pl.pw.cyber.dbaccess.testing.dsl.builders

import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneId

class MovableClock extends Clock {

    private Instant currentInstant
    private final ZoneId zone

    MovableClock(Instant fixedInstant, ZoneId zone) {
        this.currentInstant = fixedInstant
        this.zone = zone
    }

    void setInstant(Instant newInstant) {
        this.currentInstant = newInstant
    }

    void moveForward(Duration duration) {
        currentInstant = currentInstant.plus(duration)
    }

    void moveBackward(Duration duration) {
        currentInstant = currentInstant.minus(duration)
    }

    @Override
    ZoneId getZone() {
        return zone
    }

    @Override
    Clock withZone(ZoneId zone) {
        return new MovableClock(currentInstant, zone)
    }

    @Override
    Instant instant() {
        return currentInstant
    }
}
