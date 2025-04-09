package pl.pw.cyber.dbaccess.infrastructure.spring.security

import org.springframework.security.core.authority.SimpleGrantedAuthority
import spock.lang.Specification

class JwtAuthenticationTokenSpec extends Specification {

    def "should expose token as credentials"() {
        given:
            def user = "dummy-user"
            def token = "jwt-token-123"
            def authorities = [new SimpleGrantedAuthority("ROLE_USER")]

        when:
            def auth = new JwtAuthenticationToken(user, token, authorities)

        then:
            auth.getCredentials() == token
            auth.getPrincipal() == user
            auth.getAuthorities()*.authority.contains("ROLE_USER")
            auth.isAuthenticated()
    }
}
