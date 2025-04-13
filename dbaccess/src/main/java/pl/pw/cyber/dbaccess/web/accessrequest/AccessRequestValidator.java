package pl.pw.cyber.dbaccess.web.accessrequest;

import br.com.fluentvalidator.AbstractValidator;
import org.springframework.stereotype.Component;
import pl.pw.cyber.dbaccess.domain.DatabaseConfigurationProvider;

import java.util.Objects;
import java.util.Set;

@Component
public class AccessRequestValidator extends AbstractValidator<AccessRequestJson> {
    private final DatabaseConfigurationProvider provider;

    public AccessRequestValidator(DatabaseConfigurationProvider provider) {
        this.provider = provider;
    }

    @Override
    public void rules() {
        ruleFor(AccessRequestJson::permissionLevel)
          .must(Objects::nonNull)
          .withMessage("permissionLevel is required");

        ruleFor(AccessRequestJson::permissionLevel)
          .must(p -> p != null && Set.of("READ_ONLY", "READ_WRITE", "DELETE").contains(p))
          .withMessage("Invalid permissionLevel. Must be one of READ_ONLY, READ_WRITE, DELETE.");

        ruleFor(AccessRequestJson::durationMinutes)
          .must(d -> d >= 1 && d <= 60)
          .withMessage("durationMinutes must be between 1 and 60 minutes.");

        ruleFor(AccessRequestJson::targetDatabase)
          .must(Objects::nonNull)
          .withMessage("targetDatabase is required");

        ruleFor(AccessRequestJson::targetDatabase)
          .must(t -> t != null && !t.isBlank())
          .withMessage("targetDatabase must not be blank")
          .must(t -> t != null && provider.isResolvable(t))
          .withMessage("targetDatabase does not exist or is not properly configured.");
    }

}
