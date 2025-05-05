package pl.pw.cyber.dbaccess.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
class SchedulerConfig {

    @Bean
    ThreadPoolTaskScheduler taskScheduler() {
        var scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("revoke-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationMillis(5000);
        scheduler.initialize();
        return scheduler;
    }

    @Bean
    RevokeScheduler revokeScheduler(TemporaryDbAccessService temporaryDbAccessService) {
        return new ProdRevokeScheduler(temporaryDbAccessService);
    }

    @RequiredArgsConstructor
    private static class ProdRevokeScheduler implements RevokeScheduler {

        private final TemporaryDbAccessService service;

        @Override
        @Scheduled(fixedRateString = "${dbaccess.revoke-schedule-ms}")
        public void schedule() {
            service.revokeExpiredAccess();
        }
    }

}
