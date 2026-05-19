package com.challenge.notification.infrastructure.cache;

import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationChannelRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing notification channel-related caching operations.
 *
 * <p>This service provides methods to retrieve active notification channels from the database
 * and cache them for efficient retrieval. It also checks for the existence of active
 * notification channels by their code.</p>
 */
@Service
public class NotificationChannelCacheService {

    private final SpringDataNotificationChannelRepository notificationChannelRepository;

    public NotificationChannelCacheService(
            SpringDataNotificationChannelRepository notificationChannelRepository
    ) {
        this.notificationChannelRepository = notificationChannelRepository;
    }

    @Cacheable(cacheNames = NotificationCacheNames.ACTIVE_NOTIFICATION_CHANNELS)
    @Transactional(readOnly = true)
    public List<NotificationChannelEntity> getActiveNotificationChannels() {
        return notificationChannelRepository.findAllByActiveTrueOrderByNameAsc();
    }

    @Cacheable(
            cacheNames = NotificationCacheNames.ACTIVE_NOTIFICATION_CHANNELS,
            key = "'exists:' + #code"
    )
    @Transactional(readOnly = true)
    public boolean existsActiveByCode(String code) {
        return notificationChannelRepository.
                findByCodeAndActiveTrue(code)
                .isPresent();
    }
}
