package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.pw.cyber.dbaccess.web.validation.ProblemDetailsBuilder;

import java.io.IOException;
import java.util.List;
import java.util.Map;

class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache;
    private final Bandwidth policy;
    private final MeterRegistry meterRegistry;
    private final ObjectMapper objectMapper;

    private static final List<String> RATE_LIMITED_PATHS = List.of("/access-request");

    RateLimitingFilter(
      Map<String, Bucket> cache,
      Bandwidth policy,
      MeterRegistry meterRegistry,
      ObjectMapper objectMapper
    ) {
        this.cache = cache;
        this.policy = policy;
        this.meterRegistry = meterRegistry;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

        var path = request.getRequestURI();
        if (!isRateLimited(path)) {
            chain.doFilter(request, response);
            return;
        }

        var key = resolveKey();
        var bucket = cache.computeIfAbsent(key, k -> Bucket.builder().addLimit(policy).build());

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            countRateLimitExceeded(key);
            var problem = ProblemDetailsBuilder.tooManyRequest("/errors/rate-limit-exceeded");
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/problem+json");
            objectMapper.writeValue(response.getWriter(), problem);
        }
    }

    private boolean isRateLimited(String path) {
        return RATE_LIMITED_PATHS.stream().anyMatch(path::startsWith);
    }

    private String resolveKey() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "anonymous";
    }

    private void countRateLimitExceeded(String user) {
        meterRegistry.counter("rate_limit_exceeded_total", "subject", user).increment();
    }
}
