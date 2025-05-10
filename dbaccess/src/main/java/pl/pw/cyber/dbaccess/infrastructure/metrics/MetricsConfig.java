package pl.pw.cyber.dbaccess.infrastructure.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
class MetricsConfig {

    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> registry.config().commonTags(
          "application", environment.getProperty("spring.application.name", "unknown"),
          "instance", environment.getProperty("INSTANCE_ID", "localhost")
        );
    }
}
