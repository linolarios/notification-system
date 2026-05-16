package com.challenge.notification.infrastructure.cache;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.infrastructure.persistence.entity.UserCategorySubscriptionEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserChannelPreferenceEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserEntity;
import com.challenge.notification.infrastructure.persistence.mapper.NotificationSubscriberPersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataUserCategorySubscriptionRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataUserChannelPreferenceRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NotificationSubscriberCacheService {

    private final SpringDataUserCategorySubscriptionRepository categorySubscriptionRepository;
    private final SpringDataUserChannelPreferenceRepository channelPreferenceRepository;
    private final NotificationSubscriberPersistenceMapper notificationSubscriberPersistenceMapper;

    public NotificationSubscriberCacheService(
            SpringDataUserCategorySubscriptionRepository categorySubscriptionRepository,
            SpringDataUserChannelPreferenceRepository channelPreferenceRepository,
            NotificationSubscriberPersistenceMapper notificationSubscriberPersistenceMapper
    ) {
        this.categorySubscriptionRepository = categorySubscriptionRepository;
        this.channelPreferenceRepository = channelPreferenceRepository;
        this.notificationSubscriberPersistenceMapper = notificationSubscriberPersistenceMapper;
    }

    @Cacheable(
            cacheNames = NotificationCacheNames.SUBSCRIBERS_BY_CATEGORY,
            key = "#category.name()"
    )
    @Transactional(readOnly = true)
    public List<NotificationSubscriber> findActiveSubscribersByCategory(CategoryCode category) {
        List<UserCategorySubscriptionEntity> subscriptions =
                categorySubscriptionRepository.findActiveSubscriptionsByCategoryCode(category.name());

        return subscriptions
                .stream()
                .map(this::createNotificationSubscriber)
                .toList();
    }

    private NotificationSubscriber createNotificationSubscriber(UserCategorySubscriptionEntity subscription) {
        UserEntity user = subscription.getUser();

        List<UserCategorySubscriptionEntity> categorySubscriptions =
                categorySubscriptionRepository.findByIdUserId(user.getId());

        List<UserChannelPreferenceEntity> channelPreferences =
                channelPreferenceRepository.findByIdUserId(user.getId());

        return notificationSubscriberPersistenceMapper.toDomain(
                user,
                categorySubscriptions,
                channelPreferences
        );
    }
}
