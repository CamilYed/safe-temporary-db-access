package pl.pw.cyber.dbaccess.testing.dsl.abilities

import org.springframework.beans.factory.annotation.Autowired
import pl.pw.cyber.dbaccess.application.RevokeScheduler
import pl.pw.cyber.dbaccess.testing.config.TestConfig

trait SchedulingControlAbility {

    @Autowired
    private RevokeScheduler revokeScheduler

    void schedulingEnabled() {
        if (revokeScheduler instanceof TestConfig.TestRevokeScheduler) {
            ((TestConfig.TestRevokeScheduler) revokeScheduler).enable()
        }
    }

    void schedulingDisabled() {
        revokeScheduler.disable()
    }

    void manuallyTriggerScheduler() {
        revokeScheduler.schedule()
    }

}