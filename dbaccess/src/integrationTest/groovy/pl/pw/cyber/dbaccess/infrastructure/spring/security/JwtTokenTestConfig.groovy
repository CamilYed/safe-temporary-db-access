package pl.pw.cyber.dbaccess.infrastructure.spring.security

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import pl.pw.cyber.dbaccess.testing.dsl.abilities.GenerateKeysAbility

import java.security.KeyPair
import java.security.PrivateKey
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.time.Clock

@TestConfiguration
class JwtTokenTestConfig implements GenerateKeysAbility {

    @Bean
    KeyPair ecKeyPair() throws Exception {
        return generateECKeyPair()
    }

    @Bean
    PrivateKey testPrivateKey(KeyPair ecKeyPair) throws Exception {
        return ecKeyPair.private
    }

    @Bean
    @Primary
    JwtTokenVerifier jwtTokenVerifier(Clock clock, KeyPair ecKeyPair) throws Exception {
        return new JwtTokenVerifier(clock, ecKeyPair.getPublic() as ECPublicKey)
    }

    @Bean
    TestJwtTokenGenerator testJwtTokenGenerator(PrivateKey testPrivateKey) {
        return new TestJwtTokenGenerator(testPrivateKey as ECPrivateKey)
    }
}
