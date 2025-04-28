package pl.pw.cyber.dbaccess.web.accessrequest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.pw.cyber.dbaccess.application.TemporaryDbAccessService;
import pl.pw.cyber.dbaccess.application.commands.GrantTemporaryAccessCommand;
import pl.pw.cyber.dbaccess.application.results.TemporaryAccessGranted;
import pl.pw.cyber.dbaccess.domain.PermissionLevel;
import pl.pw.cyber.dbaccess.web.validation.ValidationResult.Invalid;
import pl.pw.cyber.dbaccess.web.validation.Validator;

import java.security.Principal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static pl.pw.cyber.dbaccess.web.ValidationResultMappers.TO_RESPONSE_ENTITY_MAPPER;

@RestController
@RequestMapping("/access-request")
class AccessRequestEndpoint {

    private final Validator<AccessRequestJson> accessRequestJsonValidator;
    private final TemporaryDbAccessService temporaryDbAccessService;

    AccessRequestEndpoint(
      Validator<AccessRequestJson> accessRequestJsonValidator,
      TemporaryDbAccessService temporaryDbAccessService
    ) {
        this.accessRequestJsonValidator = accessRequestJsonValidator;
        this.temporaryDbAccessService = temporaryDbAccessService;
    }

    @PostMapping
    ResponseEntity<?> requestAccess(@RequestBody AccessRequestJson accessRequestJson, Principal principal) {
        var validationResult = accessRequestJsonValidator.validate(accessRequestJson);
        if (validationResult instanceof Invalid invalid) {
            return Validator.map(invalid, TO_RESPONSE_ENTITY_MAPPER);
        }
        var command = toCommand(accessRequestJson, principal);
        var result = temporaryDbAccessService.accessRequest(command);
        return result.map(granted -> ResponseEntity.ok(
          toResponse(granted)
        )).getOrThrow();
    }

    private static GrantTemporaryAccessCommand toCommand(AccessRequestJson accessRequestJson, Principal principal) {
        return new GrantTemporaryAccessCommand(
          principal.getName(),
          accessRequestJson.targetDatabase(),
          PermissionLevel.valueOf(accessRequestJson.permissionLevel()),
          Duration.of(accessRequestJson.durationMinutes(), ChronoUnit.MINUTES)
        );
    }

    private static TemporaryAccessGrantedJson toResponse(TemporaryAccessGranted granted) {
        return new TemporaryAccessGrantedJson(
          granted.targetDatabase(),
          granted.username(),
          granted.password(),
          granted.expiresAt()
        );
    }
}
