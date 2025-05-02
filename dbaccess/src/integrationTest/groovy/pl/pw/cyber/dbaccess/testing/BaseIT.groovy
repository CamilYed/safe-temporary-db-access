package pl.pw.cyber.dbaccess.testing

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import pl.pw.cyber.dbaccess.SafeTemporaryDbAccessApplication
import pl.pw.cyber.dbaccess.infrastructure.spring.security.JwtTokenTestConfig
import pl.pw.cyber.dbaccess.testing.config.TestConfig
import pl.pw.cyber.dbaccess.testing.dsl.abilities.MakeRequestAbility
import pl.pw.cyber.dbaccess.testing.dsl.fakes.InMemoryUserRepository
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(
        useMainMethod = SpringBootTest.UseMainMethod.ALWAYS,
        webEnvironment = RANDOM_PORT,
        classes = [SafeTemporaryDbAccessApplication, TestConfig, JwtTokenTestConfig]
)
@ActiveProfiles("test")
abstract class BaseIT extends Specification implements MakeRequestAbility {

    @Value('${local.server.port}')
    private int port

    @Autowired
    private InMemoryUserRepository inMemoryUserRepository

    def setup() {
        setPort(port)
        inMemoryUserRepository.clear()
    }
}
