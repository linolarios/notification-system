package com.challenge.notification.infrastructure.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Service for managing cache eviction operations.
 *
 * <p>This service provides methods to evict cached data related to active categories,
 * notification channels, and user subscriptions. It also includes a method to evict
 * all cached data at once.</p>
 */
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
