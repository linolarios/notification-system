package com.challenge.notification.domain.service;

import com.challenge.notification.domain.model.NotificationJob;
import com.challenge.notification.domain.port.NotificationJobRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationJobRetryService {

    private static final Logger log = LoggerFactory.getLogger(NotificationJobRetryService.class);

    private final NotificationJobRepositoryPort notificationJobRepositoryPort;

    public NotificationJobRetryService(NotificationJobRepositoryPort notificationJobRepositoryPort) {
        this.notificationJobRepositoryPort = notificationJobRepositoryPort;
    }

    /**
     * Retries failed notification jobs up to a specified maximum number of attempts.
     *
     * @param maxAttempts The maximum number of attempts allowed for each job.
     * @param batchSize   The number of jobs to process in each batch.
     * @return The number of jobs retried.
     */
    @Transactional
    public int retryFailedJobs(int maxAttempts, int batchSize) {
        List<NotificationJob> failedJobs =
                notificationJobRepositoryPort.findRetryableFailedJobs(maxAttempts, batchSize);

        for (NotificationJob failedJob : failedJobs) {
            NotificationJob retryJob = failedJob.retryFailed();
            notificationJobRepositoryPort.save(retryJob);

            log.info(
                    "Retried failed notification job. jobId={} correlationId={} attemptCount={}",
                    failedJob.getId(),
                    failedJob.getCorrelationId(),
                    failedJob.getAttemptCount()
            );
        }

        return failedJobs.size();
    }
}
