package com.challenge.notification.infrastructure.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class NotificationCacheEvictionService {

    @CacheEvict(cacheNames = NotificationCacheNames.ACTIVE_CATEGORIES, allEntries = true)
    public void evictCategories() {
    }

    @CacheEvict(cacheNames = NotificationCacheNames.ACTIVE_NOTIFICATION_CHANNELS, allEntries = true)
    public void evictNotificationChannels() {
    }

    @CacheEvict(cacheNames = NotificationCacheNames.SUBSCRIBERS_BY_CATEGORY, allEntries = true)
    public void evictSubscribers() {
    }

    @CacheEvict(
            cacheNames = {
                    NotificationCacheNames.ACTIVE_CATEGORIES,
                    NotificationCacheNames.ACTIVE_NOTIFICATION_CHANNELS,
                    NotificationCacheNames.SUBSCRIBERS_BY_CATEGORY
            },
            allEntries = true
    )
    public void evictAll() {
    }
}
