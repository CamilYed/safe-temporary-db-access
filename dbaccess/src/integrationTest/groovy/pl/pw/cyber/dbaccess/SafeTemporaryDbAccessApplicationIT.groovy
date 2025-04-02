package pl.pw.cyber.dbaccess

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
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
