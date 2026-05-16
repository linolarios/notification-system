package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.port.NotificationChannelRepositoryPort;
import com.challenge.notification.infrastructure.cache.NotificationChannelCacheService;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationChannelRepository;
import org.springframework.stereotype.Repository;

@Repository
public class JpaNotificationChannelRepositoryAdapter implements NotificationChannelRepositoryPort {

    private final NotificationChannelCacheService notificationChannelCacheService;

    public JpaNotificationChannelRepositoryAdapter(NotificationChannelCacheService notificationChannelCacheService) {
        this.notificationChannelCacheService = notificationChannelCacheService;
    }

    @Override
    public boolean existsActiveByCode(NotificationChannelCode channelCode) {
        return notificationChannelCacheService.existsActiveByCode(channelCode.name());
    }
}
