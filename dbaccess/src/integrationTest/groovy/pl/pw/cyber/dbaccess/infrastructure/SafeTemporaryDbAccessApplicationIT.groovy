package pl.pw.cyber.dbaccess.infrastructure

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import pl.pw.cyber.dbaccess.testing.BaseIT

import java.security.Security

class SafeTemporaryDbAccessApplicationIT extends BaseIT {

    @Autowired
    private ApplicationContext context

    def "should load context"() {
        expect:
            context != null
    }

    def "should set crypto.policy to unlimited"() {
        expect:
            Security.getProperty("crypto.policy") == "unlimited"
    }
}
