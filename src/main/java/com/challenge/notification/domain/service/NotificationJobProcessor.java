package com.challenge.notification.domain.service;

import com.challenge.notification.config.CorrelationConstants;
import com.challenge.notification.domain.exception.NotificationJobProcessingException;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.domain.port.MessageRepositoryPort;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import com.challenge.notification.domain.port.NotificationSubscriberRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationJobProcessor {

    private static final Logger log = LoggerFactory.getLogger(NotificationJobProcessor.class);

    private final NotificationJobRepositoryPort notificationJobRepositoryPort;
    private final MessageRepositoryPort messageRepositoryPort;
    private final NotificationSubscriberRepositoryPort subscriberRepositoryPort;
    private final NotificationDispatcher notificationDispatcher;

    public NotificationJobProcessor(
            NotificationJobRepositoryPort notificationJobRepositoryPort,
            MessageRepositoryPort messageRepositoryPort,
            NotificationSubscriberRepositoryPort subscriberRepositoryPort,
            NotificationDispatcher notificationDispatcher
    ) {
        this.notificationJobRepositoryPort = notificationJobRepositoryPort;
        this.messageRepositoryPort = messageRepositoryPort;
        this.subscriberRepositoryPort = subscriberRepositoryPort;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Transactional
    public int processPendingJobs(int batchSize) {
        List<NotificationJob> pendingJobs =
                notificationJobRepositoryPort.findPendingJobsForProcessing(batchSize);

        for (NotificationJob pendingJob : pendingJobs) {
            processLockedPendingJob(pendingJob);
        }

        return pendingJobs.size();
    }

    @Transactional
    public void process(NotificationJob job) {
        processLockedPendingJob(job);
    }

    @Transactional
    public void processLockedPendingJob(NotificationJob job) {
        NotificationJob processingJob = null;

        try {
            MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, job.getCorrelationId());
            processingJob = notificationJobRepositoryPort.save(job.markProcessing());

            final Long jobId = processingJob.getId();
            NotificationMessage message = messageRepositoryPort.findById(processingJob.getMessageId())
                    .orElseThrow(
                            () -> new NotificationJobProcessingException("Message not found for job: " + jobId)
                    );

            List<NotificationSubscriber> subscribers =
                    subscriberRepositoryPort.findActiveSubscribersByCategory(message.getCategory());

            log.info(
                    "Processing notification job. jobId={} messageId={} category={} subscriberCount={}",
                    processingJob.getId(),
                    message.getId(),
                    message.getCategory(),
                    subscribers.size()
            );

            notificationDispatcher.dispatch(message, subscribers);
            notificationJobRepositoryPort.save(processingJob.markProcessed());

            log.info("Notification job processed successfully. jobId={}", job.getId());
        } catch (Exception exception) {
            log.error("Notification job processing failed. jobId={}", job.getId(), exception);

            NotificationJob failedJob = processingJob != null
                    ? processingJob.markFailed(exception.getMessage())
                    : markJobAsFailedSafely(job, exception.getMessage());

            notificationJobRepositoryPort.save(failedJob);
        } finally {
            MDC.remove(CorrelationConstants.CORRELATION_ID_MDC_KEY);
        }
    }

    /**
     * Safely marks a notification job as failed, handling different job statuses.
     *
     * @param job         the notification job to mark as failed
     * @param errorMessage the error message to associate with the failure
     * @return the updated notification job
     */
    private NotificationJob markJobAsFailedSafely(NotificationJob job, String errorMessage) {
        if (job.getStatus() == com.challenge.notification.domain.model.NotificationJobStatus.PROCESSING) {
            return job.markFailed(errorMessage);
        }

        if (job.getStatus() == com.challenge.notification.domain.model.NotificationJobStatus.PENDING) {
            return job.markProcessing().markFailed(errorMessage);
        }

        return job;
    }
}
