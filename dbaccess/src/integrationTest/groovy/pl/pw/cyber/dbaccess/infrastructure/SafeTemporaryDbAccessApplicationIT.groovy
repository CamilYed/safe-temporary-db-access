package pl.pw.cyber.dbaccess.infrastructure

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import pl.pw.cyber.dbaccess.SafeTemporaryDbAccessApplication
import spock.lang.Specification

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = SafeTemporaryDbAccessApplication)
class SafeTemporaryDbAccessApplicationIT extends Specification {

    @Autowired
    private ApplicationContext context

    def "should load context"() {
        expect:
            context != null
    }
}
