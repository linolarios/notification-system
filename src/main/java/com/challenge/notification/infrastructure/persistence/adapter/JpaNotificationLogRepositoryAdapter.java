package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.NotificationDeliveryAttempt;
import com.challenge.notification.domain.port.NotificationLogRepositoryPort;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import com.challenge.notification.infrastructure.persistence.mapper.NotificationLogPersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationChannelRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationLogRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationLogRepositoryAdapter implements NotificationLogRepositoryPort {

    private final SpringDataNotificationLogRepository notificationLogRepository;
    private final SpringDataCategoryRepository categoryRepository;
    private final SpringDataNotificationChannelRepository channelRepository;
    private final NotificationLogPersistenceMapper notificationLogPersistenceMapper;

    public JpaNotificationLogRepositoryAdapter(
            SpringDataNotificationLogRepository notificationLogRepository,
            SpringDataCategoryRepository categoryRepository,
            SpringDataNotificationChannelRepository channelRepository,
            NotificationLogPersistenceMapper notificationLogPersistenceMapper
    ) {
        this.notificationLogRepository = notificationLogRepository;
        this.categoryRepository = categoryRepository;
        this.channelRepository = channelRepository;
        this.notificationLogPersistenceMapper = notificationLogPersistenceMapper;
    }

    @Override
    public NotificationDeliveryAttempt save(NotificationDeliveryAttempt deliveryAttempt) {
        NotificationLogEntity newEntity = createNotificationLogEntity(deliveryAttempt);

        NotificationLogEntity savedEntity = notificationLogRepository.save(newEntity);

        return notificationLogPersistenceMapper.toDomain(savedEntity);
    }

    private NotificationLogEntity createNotificationLogEntity(NotificationDeliveryAttempt deliveryAttempt) {
        CategoryEntity categoryEntity = categoryRepository.findByCodeAndActiveTrue(deliveryAttempt.getCategory().name())
                .orElseThrow(() -> new IllegalArgumentException("Active category not found: " + deliveryAttempt.getCategory()));

        NotificationChannelEntity channelEntity = channelRepository.findByCodeAndActiveTrue(deliveryAttempt.getChannel().name())
                .orElseThrow(() -> new IllegalArgumentException("Active notification channel not found: " + deliveryAttempt.getChannel()));

        return notificationLogPersistenceMapper.toEntity(
                deliveryAttempt,
                categoryEntity,
                channelEntity
        );
    }

}
