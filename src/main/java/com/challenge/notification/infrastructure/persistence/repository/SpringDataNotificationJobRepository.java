package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.NotificationJobEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SpringDataNotificationJobRepository extends JpaRepository<NotificationJobEntity, Long> {


    List<NotificationJobEntity> findTop20ByStatusOrderByCreatedAtAsc(String status);

    Optional<NotificationJobEntity> findByCorrelationId(String correlationId);

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
            WHERE job.correlationId = :correlationId
            """)
    Optional<NotificationJobEntity> findByCorrelationIdWithCategory(String correlationId);

    @Query(
            value = """
                    SELECT *
                    FROM notification_jobs
                    WHERE status = 'PENDING'
                    ORDER BY created_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<NotificationJobEntity> findPendingJobsForProcessing(int limit);

    @Query(
            value = """
                    SELECT *
                    FROM notification_jobs
                    WHERE status = 'PROCESSING'
                      AND locked_at < CURRENT_TIMESTAMP - (:timeoutMinutes * INTERVAL '1 minute')
                    ORDER BY locked_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<NotificationJobEntity> findStaleProcessingJobs(
            int timeoutMinutes,
            int limit
    );

}
