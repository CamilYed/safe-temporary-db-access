package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import pl.pw.cyber.dbaccess.domain.UserRepository;

import java.io.IOException;
import java.util.List;

import static pl.pw.cyber.dbaccess.infrastructure.spring.security.SecurityConfig.AUTHORIZATION_FAILURE_ATTRIBUTE;

@Slf4j
class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtTokenVerifier jwtTokenVerifier;
    private final UserRepository userRepository;
    private static final List<SimpleGrantedAuthority> DEFAULT_ROLE = List.of(new SimpleGrantedAuthority("ROLE_REQUESTER"));


    @Autowired
    JwtAuthFilter(JwtTokenVerifier jwtTokenGenerator, UserRepository userRepository) {
        this.jwtTokenVerifier = jwtTokenGenerator;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        var authHeader = request.getHeader("Authorization");
        if (isBearer(authHeader)) {
            var token = authHeader.substring(7);
            try {
                var claims = jwtTokenVerifier.verify(token);
                var username = claims.getSubject();

                if (username == null || username.isBlank()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid token");
                    return;
                }

                var userOpt = userRepository.findBy(username);
                if (userOpt.isEmpty()) {
                    log.warn("User: {} not found", username);
                    request.setAttribute(AUTHORIZATION_FAILURE_ATTRIBUTE, true);
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden");
                    return;
                }

                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    var userDetails = userOpt.get();
                    var auth = new JwtAuthenticationToken(userDetails, token, DEFAULT_ROLE);
                    auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

                filterChain.doFilter(request, response);
            } catch (SecurityException e) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            } catch (Exception e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Authentication failure");
            }
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        }
    }

    private static boolean isBearer(String authHeader) {
        return authHeader != null && authHeader.startsWith("Bearer ");
    }
}
