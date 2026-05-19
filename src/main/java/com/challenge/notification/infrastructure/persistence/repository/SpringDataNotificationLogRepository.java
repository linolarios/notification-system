package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataNotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {

    /**
     * Returns notification logs ordered newest first for UI history and audit views.
     */
    Page<NotificationLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Returns notification logs for a single correlation ID ordered newest first.
     *
     * <p>This is useful for tracing all delivery attempts created from one submitted
     * notification request.</p>
     */
    Page<NotificationLogEntity> findByCorrelationIdOrderByCreatedAtDesc(String correlationId, Pageable pageable);

    /**
     * Searches notification logs using optional filters.
     *
     * <p>Sorting and pagination are provided by {@link Pageable}. The UI currently uses
     * this for created-at sorting and log history pagination.</p>
     */
    @Query(
            value = """
                    SELECT log
                    FROM NotificationLogEntity log
                    JOIN FETCH log.category category
                    JOIN FETCH log.channel channel
                    WHERE (:correlationIdPattern IS NULL OR LOWER(log.correlationId) LIKE :correlationIdPattern)
                      AND (:category IS NULL OR category.code = :category)
                    """,
            countQuery = """
                    SELECT COUNT(log)
                    FROM NotificationLogEntity log
                    JOIN log.category category
                    WHERE (:correlationIdPattern IS NULL OR LOWER(log.correlationId) LIKE :correlationIdPattern)
                      AND (:category IS NULL OR category.code = :category)
                    """
    )
    Page<NotificationLogEntity> searchLogs(@Param("correlationIdPattern")String correlationId, @Param("category")String category, Pageable pageable);
}

