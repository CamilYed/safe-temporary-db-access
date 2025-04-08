package pl.pw.cyber.dbaccess.testing.dsl.abilities

import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

trait GenerateKeysAbility {

    KeyPair generateECKeyPair(String curveName = "secp256r1") {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC")
        def ecSpec = new ECGenParameterSpec(curveName)
        keyGen.initialize(ecSpec)
        return keyGen.generateKeyPair()
    }

    KeyPair generateRSAKeyPair() {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }
}