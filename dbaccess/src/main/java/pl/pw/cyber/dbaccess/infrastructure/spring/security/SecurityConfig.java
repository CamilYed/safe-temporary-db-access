package pl.pw.cyber.dbaccess.infrastructure.spring.security;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.DelegatingFilterProxy;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
class SecurityConfig {
    static final String AUTHORIZATION_FAILURE_ATTRIBUTE = "AUTHORIZATION_FAILURE_ATTRIBUTE";
    private final JwtAuthFilter jwtAuthFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final Environment environment;


    SecurityConfig(
      JwtAuthFilter jwtAuthFilter,
      RateLimitingFilter rateLimitingFilter,
      Environment environment
    ) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.rateLimitingFilter = rateLimitingFilter;
        this.environment = environment;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorSecurity(HttpSecurity http) throws Exception {
        http
          .securityMatcher("/actuator/**")
          .authorizeHttpRequests(authz -> authz
            .requestMatchers("/actuator/prometheus").hasRole("METRICS_SCRAPER")
            .anyRequest().denyAll()
          )
          .httpBasic(Customizer.withDefaults())
          /**
           * CSRF is disabled only for the /actuator/prometheus endpoint,
           * which is protected by HTTP Basic and restricted to GET requests by Prometheus.
           * No sensitive state-changing operations are exposed, so the risk is mitigated.
           */
          .csrf(AbstractHttpConfigurer::disable);
        return http.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var profiles = Arrays.asList(environment.getActiveProfiles());
        boolean allowSwagger = isDevOrTestProfile(profiles) && isNotProd(profiles);

        http
          .csrf(AbstractHttpConfigurer::disable)
          .cors(AbstractHttpConfigurer::disable)
          .authorizeHttpRequests(auth -> {
              if (allowSwagger) {
                  auth.requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                  ).permitAll();
              } else {
                  auth.requestMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                  ).denyAll();
              }

              auth.anyRequest().authenticated();
          })
          .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
          .addFilterAfter(rateLimitingFilter, JwtAuthFilter.class)
          .exceptionHandling(exception -> exception
            .authenticationEntryPoint((request, response, authException) -> {
                if (request.getAttribute(AUTHORIZATION_FAILURE_ATTRIBUTE) == null) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
                }
            })
          );
        http.formLogin(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        String username = environment.getProperty("PROMETHEUS_USER", "prometheus");
        String passwordHash = environment.getProperty("PROMETHEUS_PASSWORD_HASH");

        var user = User
          .withUsername(username)
          .password("{bcrypt}" + passwordHash)
          .roles("METRICS_SCRAPER")
          .build();

        return new InMemoryUserDetailsManager(user);
    }


    private static boolean isNotProd(List<String> profiles) {
        return !profiles.contains("prod");
    }

    private static boolean isDevOrTestProfile(List<String> profiles) {
        return profiles.contains("dev") || profiles.contains("test");
    }

    @Bean
    public FilterRegistrationBean<DelegatingFilterProxy> springSecurityFilterChainRegistration() {
        FilterRegistrationBean<DelegatingFilterProxy> registration = new FilterRegistrationBean<>();
        DelegatingFilterProxy filterProxy = new DelegatingFilterProxy("springSecurityFilterChain");
        registration.setFilter(filterProxy);
        registration.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.FORWARD);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }

}
