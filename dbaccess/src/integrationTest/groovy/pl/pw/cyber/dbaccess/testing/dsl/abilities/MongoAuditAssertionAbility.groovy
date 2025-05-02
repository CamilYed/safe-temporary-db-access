package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.adapters.mongo.MongoTemplateAuditLogTestFetcher
import pl.pw.cyber.dbaccess.testing.dsl.assertions.AuditLogAssertion

trait MongoAuditAssertionAbility implements ClockControlAbility {

    @Autowired
    private MongoTemplateAuditLogTestFetcher mongoAuditLogTestFetcher

    void theAuditLog(@DelegatesTo(strategy = Closure.DELEGATE_FIRST, value = AuditLogAssertion) Closure assertions) {
        def assertion = new AuditLogAssertion(mongoAuditLogTestFetcher, testClock())
        assertions.delegate = assertion
        assertions.resolveStrategy = Closure.DELEGATE_FIRST
        assertions.call()
    }
}