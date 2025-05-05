package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.application.RevokeScheduler

trait SchedulingControlAbility {

    @Autowired
    private RevokeScheduler revokeScheduler

    void manuallyTriggerScheduler() {
        revokeScheduler.schedule()
    }
}