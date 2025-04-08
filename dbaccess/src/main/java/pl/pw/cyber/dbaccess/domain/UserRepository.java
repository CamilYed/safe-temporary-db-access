package pl.pw.cyber.dbaccess.domain;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findBy(String username);
}
