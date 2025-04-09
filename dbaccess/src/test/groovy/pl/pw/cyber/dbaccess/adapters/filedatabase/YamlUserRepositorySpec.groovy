package pl.pw.cyber.dbaccess.adapters.filedatabase

import pl.pw.cyber.dbaccess.domain.User
import spock.lang.Specification

class YamlUserRepositorySpec extends Specification {

    def "should return user if username is in allowlist"() {
        given:
            def repo = new YamlUserRepository("example-users.yaml")

        when:
            def result = repo.findBy("alice")

        then:
            result.isPresent()
            result.get() == new User("alice")
    }

    def "should return empty if username is not in allowlist"() {
        given:
            def repo = new YamlUserRepository("example-users.yam")

        when:
            def result = repo.findBy("not-in-list")

        then:
            result.isEmpty()
    }

    def "should return empty list if file is missing or invalid"() {
        given:
            def repo = new YamlUserRepository("non-existing.yaml")

        expect:
            repo.findBy("whatever").isEmpty()
    }
}
