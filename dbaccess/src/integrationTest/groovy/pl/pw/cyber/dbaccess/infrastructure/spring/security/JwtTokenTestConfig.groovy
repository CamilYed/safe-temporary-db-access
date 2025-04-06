package pl.pw.cyber.dbaccess.infrastructure.spring.security

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import pl.pw.cyber.dbaccess.testing.dsl.abilities.GenerateKeysAbility

import java.security.KeyPair
import java.security.PrivateKey
import java.security.interfaces.ECPublicKey

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
    JwtTokenVerifier jwtTokenVerifier(KeyPair ecKeyPair) throws Exception {
        return new JwtTokenVerifier(ecKeyPair.getPublic() as ECPublicKey)
    }
}
