package com.challenge.notification.domain.port;

import com.challenge.notification.domain.model.NotificationJob;

import java.util.List;
import java.util.Optional;

public interface NotificationJobRepositoryPort {

    /**
     * Persists a notification job and returns the saved domain object.
     *
     * <p>The returned job should include persistence-generated values, such as the job ID,
     * while preserving the original message ID, correlation ID, category, status, attempt
     * count, timestamps, lock metadata, and error details.</p>
     *
     * @param job notification job to persist
     * @return persisted notification job
     */
    NotificationJob save(NotificationJob job);

    Optional<NotificationJob> findById(Long id);

    /**
     * Finds pending notification jobs that are ready to be processed by a worker.
     *
     * @param limit maximum number of jobs to return
     * @return pending jobs ordered by processing priority, usually oldest first
     */
    List<NotificationJob> findPendingJobsForProcessing(int limit);

    /**
     * Finds jobs that were marked as PROCESSING but have been locked longer than the
     * configured timeout.
     *
     * <p>This supports recovery from worker crashes or unexpected shutdowns where a job
     * may have been left in PROCESSING state.</p>
     *
     * @param timeoutMinutes maximum allowed processing lock age in minutes
     * @param limit maximum number of stale jobs to return
     * @return stale processing jobs eligible for recovery
     */
    List<NotificationJob> findStaleProcessingJobs(int timeoutMinutes, int limit);

    /**
     * Finds failed jobs that can still be retried based on their attempt count.
     *
     * @param maxAttempts exclusive upper bound for retry attempts
     * @param limit maximum number of retryable jobs to return
     * @return failed jobs eligible for retry
     */
    List<NotificationJob> findRetryableFailedJobs(int maxAttempts, int limit);
}
