package pl.pw.cyber.dbaccess.testing.dsl.abilities

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.interfaces.ECPrivateKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey

trait GenerateKeysAbility {

    KeyPair generateECKeyPair() {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC")
        keyGen.initialize(256) // ES256
        return keyGen.generateKeyPair()
    }

    KeyPair generateRSAKeyPair() {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }

    ECPublicKey toECPublicKey(KeyPair pair) {
        return (ECPublicKey) pair.public
    }

    ECPrivateKey toECPrivateKey(KeyPair pair) {
        return (ECPrivateKey) pair.private
    }

    RSAPublicKey toRSAPublicKey(KeyPair pair) {
        return (RSAPublicKey) pair.public
    }

    RSAPrivateKey toRSAPrivateKey(KeyPair pair) {
        return (RSAPrivateKey) pair.private
    }
}