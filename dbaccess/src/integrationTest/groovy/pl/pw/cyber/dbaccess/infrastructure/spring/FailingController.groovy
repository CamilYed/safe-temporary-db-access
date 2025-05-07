package pl.pw.cyber.dbaccess.infrastructure.spring

import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.resource.NoResourceFoundException
import pl.pw.cyber.dbaccess.common.result.Result

@RestController
class FailingController {

    @PostMapping("/fail/illegal")
    ResponseEntity<Void> failWithIllegalArgumentException() {
        throw new IllegalArgumentException("Invalid input")
    }

    @PostMapping("/fail/no-resource-found")
    ResponseEntity<Void> failWithNoResourceFoundException() {
        throw new NoResourceFoundException(HttpMethod.GET, "/fail/no-resource-found")
    }

    @PostMapping("/fail/throwable")
    ResponseEntity<Void> failWithThrowable() {
        throw new RuntimeException("Something unexpected happened")
    }

    @PostMapping("/fail/result-execution-exception")
    ResponseEntity<Void> failWithResultExecutionException() {
        var result = Result.failure(new RuntimeException("Something unexpected happened")).getOrThrow()
        result.getOrThrow()
        return ResponseEntity<Void>.ok().build()
    }
}
