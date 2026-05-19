package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.service.NotificationJobRetryService;
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
public class NotificationJobRetryWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationJobRetryWorker.class);

    private final NotificationJobRetryService retryService;
    private final int maxAttempts;
    private final int batchSize;

    public NotificationJobRetryWorker(
            NotificationJobRetryService retryService,
            @Value("${notification.worker.retry-max-attempts:3}") int maxAttempts,
            @Value("${notification.worker.retry-batch-size:20}") int batchSize
    ) {
        this.retryService = retryService;
        this.maxAttempts = maxAttempts;
        this.batchSize = batchSize;
    }

    /**
     * Periodically retries failed notification jobs.
     *
     * <p>Exceptions are caught so that one failed retry cycle does not stop future
     * scheduled executions.</p>
     */
    @Scheduled(fixedDelayString = "${notification.worker.retry-fixed-delay-ms:60000}")
    public void retryFailedJobs() {
        try {
            int retriedCount = retryService.retryFailedJobs(maxAttempts, batchSize);

            if (retriedCount > 0) {
                log.info("Retried failed notification jobs. count={}", retriedCount);
            }
        } catch (Exception exception) {
            log.error("Unexpected notification job retry failure", exception);
        }
    }
}
