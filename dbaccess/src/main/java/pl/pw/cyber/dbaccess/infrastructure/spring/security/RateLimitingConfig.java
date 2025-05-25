package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
class RateLimitingConfig {

    @Bean
    public Map<String, Bucket> rateLimitCache() {
        return new ConcurrentHashMap<>();
    }

    @Bean
    public Bandwidth rateLimitPolicy() {
        return Bandwidth.builder()
          .capacity(5)
          .refillGreedy(5, Duration.ofMinutes(5))
          .build();
    }

    @Bean
    RateLimitingFilter rateLimitingFilter(
      Map<String, Bucket> cache,
      Bandwidth rateLimitPolicy,
      MeterRegistry meterRegistry,
      ObjectMapper objectMapper
    ) {
        return new RateLimitingFilter(cache, rateLimitPolicy, meterRegistry, objectMapper);
    }
}
