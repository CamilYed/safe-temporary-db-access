package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCrypt
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager

trait PrometheusUserAbility {

    @Autowired
    UserDetailsService userDetailsService

    def withPrometheusUser(String username, String plainPassword) {
        def passwordHash = BCrypt.hashpw(plainPassword, BCrypt.gensalt())

        System.setProperty("PROMETHEUS_USER", username)
        System.setProperty("PROMETHEUS_PASSWORD_HASH", passwordHash)
        def encoder = new BCryptPasswordEncoder()
        assert encoder.matches("secret123", passwordHash)

        def user = User.withUsername("prometheus")
                .password("{bcrypt}" + BCrypt.hashpw("secret123", BCrypt.gensalt()))
                .roles("METRICS_SCRAPER")
                .build()
        ((InMemoryUserDetailsManager)userDetailsService).updateUser(user)
    }
}
