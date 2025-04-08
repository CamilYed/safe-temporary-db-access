package pl.pw.cyber.dbaccess.adapters.filedatabase;

import org.yaml.snakeyaml.Yaml;
import pl.pw.cyber.dbaccess.domain.User;
import pl.pw.cyber.dbaccess.domain.UserRepository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class YamlUserRepository implements UserRepository {

    private final List<String> allowlist;

    YamlUserRepository() {
        this.allowlist = loadAllowlistFromFile();
    }

    @Override
    public Optional<User> findBy(String username) {
        return allowlist.contains(username)
          ? Optional.of(new User(username))
          : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private List<String> loadAllowlistFromFile() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("example-users.yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);
            return (List<String>) data.get("allowlist");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load allowlist from example-users.yaml", e);
        }
    }
}
