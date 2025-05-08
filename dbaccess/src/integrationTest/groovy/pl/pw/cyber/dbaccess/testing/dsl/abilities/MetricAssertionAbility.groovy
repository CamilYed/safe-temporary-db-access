package pl.pw.cyber.dbaccess.testing.dsl.abilities

import io.micrometer.core.instrument.Meter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.search.RequiredSearch
import org.springframework.beans.factory.annotation.Autowired

trait MetricAssertionAbility {

    @Autowired
    private MeterRegistry meterRegistry

    void metricWasExposed(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MetricAssertion) Closure<?> block) {
        def assertion = new MetricAssertion(meterRegistry)
        block.delegate = assertion
        block.resolveStrategy = Closure.DELEGATE_FIRST
        block.call()
        assertion.validate()
    }

    void metricWasNotExposed(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = MetricAssertionBuilder) Closure<?> config) {
        def builder = new MetricAssertionBuilder(meterRegistry)
        config.delegate = builder
        config.resolveStrategy = Closure.DELEGATE_FIRST
        config.call()
        builder.assertNotExposed()
    }

    static class MetricAssertion {
        private final MeterRegistry registry
        private String metricName
        private final Map<String, String> requiredTags = [:]
        private Double expectedMinValue = null

        MetricAssertion(MeterRegistry registry) {
            this.registry = registry
        }

        void hasName(String name) {
            this.metricName = name
        }

        void hasTag(String key, String value) {
            requiredTags[key] = value
        }

        void hasValueGreaterThan(double minValue) {
            this.expectedMinValue = minValue
        }

        void validate() {
            assert metricName != null : "Metric name must be defined"

            RequiredSearch meters = registry.get(metricName)
            if (!requiredTags.isEmpty()) {
                meters = meters.tags(requiredTags.collectMany { [it.key, it.value] } as String[])
            }

            def counter = meters.counter()
            assert counter != null : "Expected metric '${metricName}' not found"
            assert counter.count() > (expectedMinValue ?: 0) : "Expected value > ${expectedMinValue}, but was ${counter.count()}"
        }
    }

    static class MetricAssertionBuilder {
        private final MeterRegistry registry
        private String metricName
        private final Map<String, String> requiredTags = [:]

        MetricAssertionBuilder(MeterRegistry registry) {
            this.registry = registry
        }

        void hasName(String name) {
            this.metricName = name
        }

        void hasTag(String key, String value) {
            requiredTags[key] = value
        }

        void assertNotExposed() {
            assert metricName != null : "Metric name must be defined"

            List<Meter> matching = registry.getMeters().stream()
                    .filter { m -> m.getId().getName() == metricName }
                    .filter { m -> requiredTags.entrySet().every { tag ->
                        m.getId().getTags().any { t -> t.key == tag.key && t.value == tag.value }
                    }}
                    .toList()

            assert matching.isEmpty(): "Expected no metric '${metricName}' with tags ${requiredTags}, but found: $matching"
        }
    }
}
