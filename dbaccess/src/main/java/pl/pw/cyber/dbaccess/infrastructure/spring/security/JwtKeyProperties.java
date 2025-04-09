package pl.pw.cyber.dbaccess.infrastructure.spring.security;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;


@ConfigurationProperties(prefix = "jwt")
record JwtKeyProperties(Resource publicKey) {

    public static JwtKeyProperties of(Resource publicKey) {
        return new JwtKeyProperties(publicKey);
    }
}
