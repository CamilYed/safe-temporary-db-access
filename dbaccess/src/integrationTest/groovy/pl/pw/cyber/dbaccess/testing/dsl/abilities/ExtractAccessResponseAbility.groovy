package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.http.ResponseEntity
import pl.pw.cyber.dbaccess.web.accessrequest.TemporaryAccessGrantedJson

import java.time.Instant

trait ExtractAccessResponseAbility {

    TemporaryAccessGrantedJson extractFromResponse(ResponseEntity<Map> response) {
        def body = response.body
        assert body.username != null
        assert body.password != null
        assert body.expiresAt != null

        return new TemporaryAccessGrantedJson(
                body.username as String,
                body.password as String,
                Instant.parse(body.expiresAt as String)
        )
    }
}

