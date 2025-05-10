package pl.pw.cyber.dbaccess.infrastructure.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import pl.pw.cyber.dbaccess.testing.MongoBaseIT

class MetricsConfigIT extends MongoBaseIT {

    @Autowired
    MeterRegistry meterRegistry

    @Autowired
    Environment environment

    def "should register common tags for created metrics"() {
        given:
            def counter = meterRegistry.counter("test.metric", "customTag", "123")
            counter.increment()

        when:
            def meter = meterRegistry.get("test.metric").counter()

        then:
            meter.id.getTag("customTag") == "123"
            meter.id.getTag("application") == "safe-temporary-db-access"
            meter.id.getTag("instance") == "localhost"
    }
}
