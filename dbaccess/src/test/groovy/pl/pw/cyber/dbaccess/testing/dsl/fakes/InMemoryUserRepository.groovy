package pl.pw.cyber.dbaccess.testing.dsl.fakes

import pl.pw.cyber.dbaccess.domain.User
import pl.pw.cyber.dbaccess.domain.UserRepository

class InMemoryUserRepository implements UserRepository {

    private final Map<String, User> USERS = [:]

    @Override
    Optional<User> findBy(String username) {
        return Optional.ofNullable(USERS.get(username))
    }

    void addUser(User user) {
        USERS.put(user.username(), user)
    }

    void remove(String name) {
        USERS.remove(name)
    }

    void clear() {
        USERS.clear()
    }
}
