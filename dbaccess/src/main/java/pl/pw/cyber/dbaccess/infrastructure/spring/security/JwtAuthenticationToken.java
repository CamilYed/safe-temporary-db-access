package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import pl.pw.cyber.dbaccess.domain.User;

import java.util.Collection;

class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private final User principal;
    private final String token;

    public JwtAuthenticationToken(User principal, String token, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        setAuthenticated(true);
    }

    public JwtAuthenticationToken(String principal, String token, Collection<? extends GrantedAuthority> authorities) {
        this(new User(principal), token, authorities);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal.username();
    }
}
