package pl.pw.cyber.dbaccess.testing.dsl.abilities

import io.github.bucket4j.Bucket
import org.springframework.beans.factory.annotation.Autowired

trait RateLimiterCleanerAbility {

    @Autowired
    private Map<String, Bucket> rateLimitCache

    void clearRateLimiterCache() {
        if (rateLimitCache != null) {
            rateLimitCache.clear()
        }
    }
}
