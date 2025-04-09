package pl.pw.cyber.dbaccess.infrastructure.spring.security;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

import java.time.Duration;


@ConfigurationProperties(prefix = "jwt")
record JwtKeyProperties(
  Resource publicKey,
  Duration ttl
) {

    static final Duration DEFAULT_TTL_DURATION = Duration.ofMinutes(5);
    static final String ISSUER = "dbaccess-api";
    static final String AUDIENCE = "dbaccess-client";

    public JwtKeyProperties(Resource publicKey, Duration ttl) {
        this.publicKey = publicKey;
        this.ttl = ttl != null ? ttl : DEFAULT_TTL_DURATION;
    }
}
