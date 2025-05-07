package pl.pw.cyber.dbaccess.infrastructure.spring.security

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityScheme
import spock.lang.Specification

class OpenApiConfigSpec extends Specification {

    def "should return OpenAPI bean with proper configuration"() {
        given:
            def config = new OpenApiConfig()

        when:
            OpenAPI openAPI = config.customOpenAPI()

        then:
            openAPI.info.title == "SafeTemporaryDbAccess API"
            openAPI.info.version == "1.0"
            openAPI.components.securitySchemes.get("BearerAuth").type == SecurityScheme.Type.HTTP
            openAPI.components.securitySchemes.get("BearerAuth").scheme == "bearer"
            openAPI.components.securitySchemes.get("BearerAuth").bearerFormat == "JWT"
    }
}
