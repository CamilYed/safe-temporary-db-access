package pl.pw.cyber.dbaccess.domain

import spock.lang.Specification

class TemporaryCredentialsSpec extends Specification {

    def "should create valid TemporaryCredentials"() {
        when:
            def creds = new TemporaryCredentials("user123", "StrongPass1!")

        then:
            creds.username() == "user123"
            creds.password() == "StrongPass1!"
    }

    def "should fail on invalid usernames"() {
        when:
            new TemporaryCredentials(invalidUsername, "StrongPass1!")

        then:
            thrown(IllegalArgumentException)

        where:
            invalidUsername << [null, "", "abc", "ABC123", "user_123", "user!@#"]
    }

    def "should fail on invalid passwords"() {
        when:
            new TemporaryCredentials("user123", invalidPassword)

        then:
            thrown(IllegalArgumentException)

        where:
            invalidPassword << [
                    null,
                    "short1A!",
                    "alllowercase1!",
                    "ALLUPPERCASE1!",
                    "NoSpecial123",
                    "NoDigits!!!",
                    "nocapsanddigits123"
            ]
    }
}

