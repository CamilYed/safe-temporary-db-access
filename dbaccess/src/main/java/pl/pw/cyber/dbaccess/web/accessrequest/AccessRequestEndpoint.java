package pl.pw.cyber.dbaccess.web.accessrequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pw.cyber.dbaccess.web.validation.Validator;

import static pl.pw.cyber.dbaccess.web.ValidationResultMappers.TO_RESPONSE_ENTITY_MAPPER;

@RestController
@RequestMapping("/access-request")
public class AccessRequestEndpoint {

    private final Validator<AccessRequestJson> accessRequestJsonValidator;

    public AccessRequestEndpoint(Validator<AccessRequestJson> accessRequestJsonValidator) {
        this.accessRequestJsonValidator = accessRequestJsonValidator;
    }


    @PostMapping
    ResponseEntity<?> requestAccess(@RequestBody AccessRequestJson accessRequestJson) {
        return accessRequestJsonValidator.validateAndMap(accessRequestJson, TO_RESPONSE_ENTITY_MAPPER);
    }

}
