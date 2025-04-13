package pl.pw.cyber.dbaccess.adapters.fluentvalidator;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.pw.cyber.dbaccess.web.accessrequest.AccessRequestJson;
import pl.pw.cyber.dbaccess.web.accessrequest.AccessRequestValidator;
import pl.pw.cyber.dbaccess.web.validation.Validator;

@Configuration
class ValidationConfig {

    @Bean
    Validator<AccessRequestJson> accessRequestValidatorAdapter(AccessRequestValidator accessRequestValidator) {
        return new ValidationAdapter<>(accessRequestValidator);
    }
}
