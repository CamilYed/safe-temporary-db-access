package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
class SecurityConfig {
    static final String AUTHORIZATION_FAILURE_ATTRIBUTE = "AUTHORIZATION_FAILURE_ATTRIBUTE";
    private final JwtAuthFilter jwtAuthFilter;

    SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
          .csrf(AbstractHttpConfigurer::disable)
          .cors(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
          .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                if (request.getAttribute(AUTHORIZATION_FAILURE_ATTRIBUTE) == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                }
            })
          );
        return http.build();
    }
}
