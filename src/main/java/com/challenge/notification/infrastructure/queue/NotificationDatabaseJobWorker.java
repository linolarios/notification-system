package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.domain.service.NotificationJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Conditional(DatabaseProcessingWorkerEnabledCondition.class)
public class NotificationDatabaseJobWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationDatabaseJobWorker.class);

    private final NotificationJobProcessor notificationJobProcessor;
    private final int batchSize;

    public NotificationDatabaseJobWorker(
            NotificationJobProcessor notificationJobProcessor,
            @Value("${notification.worker.batch-size:20}")
            int batchSize
    ) {
        this.notificationJobProcessor = notificationJobProcessor;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${notification.worker.fixed-delay-ms:1000}")
    public void processPendingJobs() {
        try {
            int processedCount = notificationJobProcessor.processPendingJobs(batchSize);

            if (processedCount > 0) {
                log.info("Processed pending notification jobs. count={}", processedCount);
            }
        } catch (Exception exception) {
            log.error("Unexpected database job worker failure", exception);
        }
    }
}
