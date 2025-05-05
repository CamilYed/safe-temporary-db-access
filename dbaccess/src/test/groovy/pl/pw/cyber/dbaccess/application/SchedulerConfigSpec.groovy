package pl.pw.cyber.dbaccess.application


import spock.lang.Specification

class SchedulerConfigSpec extends Specification {

    def "should create task scheduler with expected settings"() {
        given:
            def config = new SchedulerConfig()

        when:
            def scheduler = config.taskScheduler()
            scheduler.initialize()

        then:
            scheduler.threadNamePrefix == "revoke-scheduler-"
            scheduler.scheduledExecutor.corePoolSize == 2
    }

    def "should create revoke scheduler and call revokeExpiredAccess"() {
        given:
            def service = Mock(TemporaryDbAccessService)
            def config = new SchedulerConfig()

        when:
            def scheduler = config.revokeScheduler(service)

        then:
            scheduler != null

        when:
            scheduler.schedule()

        then:
            1 * service.revokeExpiredAccess()
    }
}
