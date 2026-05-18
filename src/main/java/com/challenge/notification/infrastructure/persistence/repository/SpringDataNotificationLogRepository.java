package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataNotificationLogRepository extends JpaRepository<NotificationLogEntity, Long> {

    Page<NotificationLogEntity> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<NotificationLogEntity> findByCorrelationIdOrderByCreatedAtDesc(String correlationId, Pageable pageable);
}

