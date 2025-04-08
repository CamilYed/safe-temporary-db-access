package pl.pw.cyber.dbaccess.infrastructure.spring

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class FailingController {

    @PostMapping("/fail/illegal")
    ResponseEntity<Void> failWithIllegalArgumentException() {
        throw new IllegalArgumentException("Invalid input")
    }

    @PostMapping("/fail/throwable")
    ResponseEntity<Void> failWithThrowable() {
        throw new RuntimeException("Something unexpected happened")
    }
}
