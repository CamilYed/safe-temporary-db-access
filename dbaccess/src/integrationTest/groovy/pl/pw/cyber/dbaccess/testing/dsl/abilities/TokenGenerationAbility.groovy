package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.infrastructure.spring.security.TestJwtTokenGenerator
import pl.pw.cyber.dbaccess.testing.dsl.builders.TestTokenBuilder

trait TokenGenerationAbility {

    @Autowired
    private TestJwtTokenGenerator testJwtTokenGenerator

    String generateToken(TestTokenBuilder builder) {
        return testJwtTokenGenerator.generateToken(builder)
    }
}