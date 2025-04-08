package pl.pw.cyber.dbaccess.testing.dsl.abilities

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import org.slf4j.LoggerFactory

trait LogCaptureAbility {
    private InMemoryAppender inMemoryAppender
    private Logger loggerUnderTest

    void setupLogCapture(String loggerName) {
        loggerUnderTest = (Logger) LoggerFactory.getLogger(loggerName)
        inMemoryAppender = new InMemoryAppender()
        inMemoryAppender.start()
        loggerUnderTest.addAppender(inMemoryAppender)
    }

    void cleanupLogCapture() {
        if (loggerUnderTest && inMemoryAppender) {
            loggerUnderTest.detachAppender(inMemoryAppender)
            inMemoryAppender.stop()
        }
    }

    List<ILoggingEvent> getCapturedLogs() {
        return inMemoryAppender?.events ?: []
    }

    void warnLogCaptured(String partialMessage) {
        logCaptured(partialMessage, Level.WARN)
    }

    void logCaptured(String expectedMessage, Level expectedLevel) {
        assert capturedLogs.any { it.formattedMessage.contains(expectedMessage) && it.level == expectedLevel }
    }
}

class InMemoryAppender extends AppenderBase<ILoggingEvent> {
    private final List<ILoggingEvent> events = []

    @Override
    protected void append(ILoggingEvent eventObject) {
        events.add(eventObject)
    }

    List<ILoggingEvent> getEvents() {
        return events
    }
}
