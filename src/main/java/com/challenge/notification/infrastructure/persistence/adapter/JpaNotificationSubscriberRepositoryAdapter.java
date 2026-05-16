package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.domain.port.NotificationSubscriberRepositoryPort;
import com.challenge.notification.infrastructure.cache.NotificationSubscriberCacheService;
import com.challenge.notification.infrastructure.persistence.entity.UserCategorySubscriptionEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserChannelPreferenceEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserEntity;
import com.challenge.notification.infrastructure.persistence.mapper.NotificationSubscriberPersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataUserCategorySubscriptionRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataUserChannelPreferenceRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataUserRepository;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
public class JpaNotificationSubscriberRepositoryAdapter implements NotificationSubscriberRepositoryPort {

    private final NotificationSubscriberCacheService notificationSubscriberCacheService;

    public JpaNotificationSubscriberRepositoryAdapter(NotificationSubscriberCacheService notificationSubscriberCacheService) {
        this.notificationSubscriberCacheService = notificationSubscriberCacheService;
    }

    @Override
    public List<NotificationSubscriber> findActiveSubscribersByCategory(CategoryCode category) {
        return notificationSubscriberCacheService.findActiveSubscribersByCategory(category);
    }
}
