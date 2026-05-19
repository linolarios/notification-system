package com.challenge.notification.domain.service;

import com.challenge.notification.config.CorrelationConstants;
import com.challenge.notification.domain.exception.NotificationJobProcessingException;
import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.model.NotificationJobStatus;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.domain.port.MessageRepositoryPort;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import com.challenge.notification.domain.port.NotificationSubscriberRepositoryPort;
import org.jspecify.annotations.NonNull;
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
            processCorrelationId(job);
            processingJob = notificationJobRepositoryPort.save(job.markProcessing());
            ProcessJob(processingJob);

            log.info("Notification job processed successfully. jobId={}", job.getId());
        } catch (Exception exception) {
            persistFailedJob(job, exception, processingJob);
        } finally {
            clearCorrelationId();
        }
    }

    private static void clearCorrelationId() {
        MDC.remove(CorrelationConstants.CORRELATION_ID_MDC_KEY);
    }

    private void ProcessJob(NotificationJob processingJob) {
        NotificationMessage message = retrieveNotificationMessage(processingJob);

        List<NotificationSubscriber> subscribers =
                subscriberRepositoryPort.findActiveSubscribersByCategory(message.getCategory());

        logProcessingStarted(message, processingJob, subscribers);

        notificationDispatcher.dispatch(message, subscribers);
        notificationJobRepositoryPort.save(processingJob.markProcessed());
    }

    private static void logProcessingStarted(NotificationMessage message, NotificationJob processingJob, List<NotificationSubscriber> subscribers) {
        log.info(
                "Processing notification job. jobId={} messageId={} category={} subscriberCount={}",
                processingJob.getId(),
                message.getId(),
                message.getCategory(),
                subscribers.size()
        );
    }

    private static void processCorrelationId(NotificationJob job) {
        MDC.put(CorrelationConstants.CORRELATION_ID_MDC_KEY, job.getCorrelationId());
    }

    private void persistFailedJob(NotificationJob job, Exception exception, NotificationJob processingJob) {
        log.error("Notification job processing failed. jobId={}", job.getId(), exception);
        String errorMessage = resolveErrorMessage(exception);

        NotificationJob failedJob = processingJob != null
                ? processingJob.markFailed(errorMessage)
                : markJobAsFailedSafely(job, errorMessage);

        notificationJobRepositoryPort.save(failedJob);
    }

    private String resolveErrorMessage(Exception exception) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            return exception.getClass().getSimpleName();
        }

        return message;
    }

    private @NonNull NotificationMessage retrieveNotificationMessage(NotificationJob processingJob) {
        final Long jobId = processingJob.getId();
        return messageRepositoryPort.findById(processingJob.getMessageId())
                .orElseThrow(
                        () -> new NotificationJobProcessingException("Message not found for job: " + jobId)
                );
    }

    /**
     * Safely marks a notification job as failed, handling different job statuses.
     *
     * @param job          the notification job to mark as failed
     * @param errorMessage the error message to associate with the failure
     * @return the updated notification job
     */
    private NotificationJob markJobAsFailedSafely(NotificationJob job, String errorMessage) {
        if (job.getStatus() == NotificationJobStatus.PROCESSING) {
            return job.markFailed(errorMessage);
        }

        if (job.getStatus() == NotificationJobStatus.PENDING) {
            return job.markProcessing().markFailed(errorMessage);
        }

        throw new IllegalStateException("Cannot mark notification job as failed from status: " + job.getStatus());
    }
}
