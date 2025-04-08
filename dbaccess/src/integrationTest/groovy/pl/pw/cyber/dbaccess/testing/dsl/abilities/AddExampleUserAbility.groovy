package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.domain.User
import pl.pw.cyber.dbaccess.testing.dsl.fakes.InMemoryUserRepository

trait AddExampleUserAbility {

    @Autowired
    private InMemoryUserRepository inMemoryUserRepository

    void thereIsUser(String name) {
        inMemoryUserRepository.addUser(new User(name))
    }

    void userWithNameDoesNotExists(String name) {
        inMemoryUserRepository.remove(name)
    }
}