package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataNotificationJobRepository extends JpaRepository<NotificationJobEntity, Long> {

    Optional<NotificationJobEntity> findByCorrelationId(String correlationId);

    /**
     * Fetches notification jobs with their category relationship initialized.
     *
     * <p>This avoids lazy-loading issues when mapping JPA entities back to domain models
     * outside the repository boundary.</p>
     */
    @Query("""
            SELECT job
            FROM NotificationJobEntity job
            JOIN FETCH job.category category
            WHERE job.id = :id
            """)
    Optional<NotificationJobEntity> findByIdWithCategory(Long id);

    @Query("""
            SELECT job
            FROM NotificationJobEntity job
            JOIN FETCH job.category category
            WHERE job.id IN :ids
            """)
    List<NotificationJobEntity> findAllByIdInWithCategory(List<Long> ids);

    /**
     * Finds pending job IDs for worker processing using row-level locking.
     *
     * <p>{@code FOR UPDATE SKIP LOCKED} allows multiple application instances or worker
     * threads to safely poll pending jobs without processing the same row twice.</p>
     */
    @Query(
            value = """
                    SELECT id
                    FROM notification_jobs
                    WHERE status = 'PENDING'
                    ORDER BY created_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<Long> findPendingJobIdsForProcessing(int limit);

    /**
     * Finds PROCESSING jobs whose lock is older than the configured timeout.
     *
     * <p>These jobs are candidates for recovery because the worker that locked them may
     * have crashed or stopped before completing the job.</p>
     */
    @Query(
            value = """
                    SELECT id
                    FROM notification_jobs
                    WHERE status = 'PROCESSING'
                      AND locked_at < CURRENT_TIMESTAMP - (:timeoutMinutes * INTERVAL '1 minute')
                    ORDER BY locked_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<Long> findStaleProcessingJobIds(int timeoutMinutes, int limit);

    /**
     * Finds FAILED jobs that have not reached the maximum retry attempt count.
     *
     * <p>The query locks selected rows so concurrent retry workers do not retry the same
     * failed job simultaneously.</p>
     */
    @Query(
            value = """
                    SELECT id
                    FROM notification_jobs
                    WHERE status = 'FAILED'
                      AND attempt_count < :maxAttempts
                    ORDER BY updated_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<Long> findRetryableFailedJobIds(int maxAttempts, int limit);
}
