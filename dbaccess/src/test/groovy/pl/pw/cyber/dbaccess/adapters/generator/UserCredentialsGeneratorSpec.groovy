package pl.pw.cyber.dbaccess.adapters.generator


import pl.pw.cyber.dbaccess.domain.TemporaryCredentials
import pl.pw.cyber.dbaccess.testing.dsl.abilities.EntropyCalculateAbility
import spock.lang.Specification

class UserCredentialsGeneratorSpec extends Specification implements EntropyCalculateAbility {

    def generator = new SecureUserCredentialsGenerator()

    def "should generate valid credentials"() {
        when:
            TemporaryCredentials creds = generator.generate()

        then:
            isValidUsername(creds.username())
            isValidPassword(creds.password())
    }

    def "should generate password with sufficient entropy"() {
        expect:
            (1..100).each {
                def password = generator.generate().password()
                def entropy = shannonEntropy(password)
                println "Entropy of $password = ${String.format('%.2f', entropy)}"
                assert entropy >= 55.0 : "Entropy is below 55.0 for: $password"
            }
    }

    private static boolean isValidUsername(String username) {
        return username ==~ /^[a-z0-9]{6,}$/
    }

    private static boolean isValidPassword(String password) {
        return password ==~ /^(?=.*[A-Z])(?=.*[a-z])(?=.*\d)(?=.*[^a-zA-Z0-9]).{12,}$/
    }
}