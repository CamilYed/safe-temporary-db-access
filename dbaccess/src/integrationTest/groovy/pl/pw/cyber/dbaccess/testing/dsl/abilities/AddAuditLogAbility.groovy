package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.domain.TemporaryAccessAuditLogRepository
import pl.pw.cyber.dbaccess.testing.dsl.builders.TemporaryAccessAuditLogBuilder

trait AddAuditLogAbility {

    @Autowired
    private TemporaryAccessAuditLogRepository repository

    void thereIs(TemporaryAccessAuditLogBuilder auditLog) {
        repository.logTemporaryAccess(auditLog.build())
    }
}