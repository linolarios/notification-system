package com.challenge.notification.infrastructure.persistence.repository;

import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataNotificationChannelRepository extends JpaRepository<NotificationChannelEntity, Short> {

    Optional<NotificationChannelEntity> findByCodeAndActiveTrue(String code);

    List<NotificationChannelEntity> findAllByActiveTrueOrderByNameAsc();
}
