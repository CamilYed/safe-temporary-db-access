package pl.pw.cyber.dbaccess.adapters.filedatabase;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.Yaml;
import pl.pw.cyber.dbaccess.domain.User;
import pl.pw.cyber.dbaccess.domain.UserRepository;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
class YamlUserRepository implements UserRepository {

    private final List<String> allowlist;

    YamlUserRepository(String allowlist) {
        this.allowlist = loadAllowlistFromFile(allowlist);
    }

    @Override
    public Optional<User> findBy(String username) {
        return allowlist.contains(username)
          ? Optional.of(new User(username))
          : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private List<String> loadAllowlistFromFile(String allowlist) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(allowlist)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(input);
            return (List<String>) data.get("allowlist");
        } catch (Exception e) {
            log.error("Error loading allowlist from file", e);
            return List.of();
        }
    }
}
