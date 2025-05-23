package pl.pw.cyber.dbaccess.testing.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import pl.pw.cyber.dbaccess.application.RevokeScheduler
import pl.pw.cyber.dbaccess.application.TemporaryDbAccessService
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider
import pl.pw.cyber.dbaccess.domain.UserRepository
import pl.pw.cyber.dbaccess.testing.dsl.builders.MovableClock
import pl.pw.cyber.dbaccess.testing.dsl.fakes.FakeDatabaseConfigurationProvider
import pl.pw.cyber.dbaccess.testing.dsl.fakes.InMemoryUserRepository

import java.time.Instant
import java.time.ZoneId

@TestConfiguration
class TestConfig {

    @Bean
    @Primary
    UserRepository userRepository() {
        return new InMemoryUserRepository()
    }

    @Bean
    @Primary
    MovableClock movableClock() {
        return new MovableClock(Instant.parse("2025-04-07T12:00:00Z"), ZoneId.of("UTC"))
    }

    @Bean
    @Primary
    DatabaseConfigurationProvider databaseConfigurationProvider() {
        return new FakeDatabaseConfigurationProvider()
    }

    @Bean
    @Primary
    RevokeScheduler revokeScheduler(TemporaryDbAccessService service) {
        return new TestRevokeScheduler(service)
    }

    static class TestRevokeScheduler implements RevokeScheduler {

        private final TemporaryDbAccessService service

        TestRevokeScheduler(TemporaryDbAccessService service) {
            this.service = service
        }

        @Override
        void schedule() {
            println "Test scheduler manually triggering revokeExpiredAccess"
            service.revokeExpiredAccess()
        }
    }
}
