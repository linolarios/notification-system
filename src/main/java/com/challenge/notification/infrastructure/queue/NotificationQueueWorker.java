package com.challenge.notification.infrastructure.queue;

import com.challenge.notification.config.CorrelationConstants;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.port.NotificationQueue;
import com.challenge.notification.domain.service.NotificationJobProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Conditional(MemoryProcessingWorkerEnabledCondition.class)
public class NotificationQueueWorker {

    private static final Logger log = LoggerFactory.getLogger(NotificationQueueWorker.class);

    private final NotificationQueue notificationQueue;
    private final NotificationJobProcessor notificationJobProcessor;
    private final int batchSize;

    public NotificationQueueWorker(
            NotificationQueue notificationQueue,
            NotificationJobProcessor notificationJobProcessor,
            @Value("${notification.worker.batch-size:20}") int batchSize
    ) {
        this.notificationQueue = notificationQueue;
        this.notificationJobProcessor = notificationJobProcessor;
        this.batchSize = batchSize;
    }

    /**
     * Periodically polls the notification queue for jobs and processes them.
     *
     * <p>Exceptions are caught so that one failed polling cycle does not stop future
     * scheduled executions.</p>
     */
    @Scheduled(fixedDelayString = "${notification.worker.fixed-delay-ms:1000}")
    public void pollQueue() {
        for (int i = 0; i < batchSize; i++) {
            Optional<NotificationJob> polledJob = notificationQueue.poll();

            if (polledJob.isEmpty()) {
                return;
            }
            processJobSafely(polledJob.get());
        }
    }

    private void processJobSafely(NotificationJob job) {
        try {
            MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, job.getCorrelationId());
            notificationJobProcessor.process(job);
        } catch (Exception exception) {
            log.error("Unexpected worker failure while processing job. jobId={}", job.getId(), exception);
        } finally {
            MDC.remove(CorrelationConstants.CORRELATION_ID_MDC_KEY);
        }
    }
}
