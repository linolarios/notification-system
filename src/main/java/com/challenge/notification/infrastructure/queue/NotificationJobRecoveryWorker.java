package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.service.NotificationJobRecoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        prefix = "notification.worker",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class NotificationJobRecoveryWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationJobRecoveryWorker.class);

    private final NotificationJobRecoveryService recoveryService;
    private final int timeoutMinutes;
    private final int batchSize;

    public NotificationJobRecoveryWorker(
            NotificationJobRecoveryService recoveryService,
            @Value("${notification.worker.stale-timeout-minutes:5}") int timeoutMinutes,
            @Value("${notification.worker.recovery-batch-size:20}") int batchSize
    ) {
        this.recoveryService = recoveryService;
        this.timeoutMinutes = timeoutMinutes;
        this.batchSize = batchSize;
    }

    /**
     * Periodically recovers stale notification jobs that have been stuck in processing state.
     *
     * <p>Exceptions are caught so that one failed recovery cycle does not stop future
     * scheduled executions.</p>
     */
    @Scheduled(fixedDelayString = "${notification.worker.recovery-fixed-delay-ms:30000}")
    public void recoverStaleJobs() {
        try {
            int recoveredCount = recoveryService.recoverStaleProcessingJobs(
                    timeoutMinutes,
                    batchSize
            );

            if (recoveredCount > 0) {
                log.warn("Recovered stale notification jobs. count={}", recoveredCount);
            }
        } catch (Exception exception) {
            log.error("Unexpected notification job recovery failure", exception);
        }
    }
}
