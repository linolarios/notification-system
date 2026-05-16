package com.challenge.notification.infrastructure.persistence.mapper;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.infrastructure.persistence.entity.UserCategorySubscriptionEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserChannelPreferenceEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class NotificationSubscriberPersistenceMapper {

    public NotificationSubscriber toDomain(
            UserEntity userEntity,
            List<UserCategorySubscriptionEntity> categorySubscriptions,
            List<UserChannelPreferenceEntity> channelPreferences
    ) {
        Set<CategoryCode> subscribedCategories = categorySubscriptions.stream()
                .map(subscription -> CategoryCode.from(subscription.getCategory().getCode()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(CategoryCode.class)));

        Set<NotificationChannelCode> enabledChannels = channelPreferences.stream()
                .map(preference -> NotificationChannelCode.from(preference.getChannel().getCode()))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(NotificationChannelCode.class)));

        return new NotificationSubscriber(
                userEntity.getId(),
                userEntity.getName(),
                userEntity.getEmail(),
                userEntity.getPhoneNumber(),
                subscribedCategories,
                enabledChannels
        );
    }
}
