package com.challenge.notification.infrastructure.cache;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.infrastructure.persistence.entity.UserCategorySubscriptionEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserChannelPreferenceEntity;
import com.challenge.notification.infrastructure.persistence.entity.UserEntity;
import com.challenge.notification.infrastructure.persistence.mapper.NotificationSubscriberPersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataUserCategorySubscriptionRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataUserChannelPreferenceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSubscriberCacheServiceTest {

    @Mock
    private SpringDataUserCategorySubscriptionRepository categorySubscriptionRepository;

    @Mock
    private SpringDataUserChannelPreferenceRepository channelPreferenceRepository;

    @Mock
    private NotificationSubscriberPersistenceMapper notificationSubscriberPersistenceMapper;

    @Mock
    private UserCategorySubscriptionEntity sportsSubscription;

    @Mock
    private UserChannelPreferenceEntity emailPreference;

    @Mock
    private UserEntity user;

    private NotificationSubscriberCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new NotificationSubscriberCacheService(
                categorySubscriptionRepository,
                channelPreferenceRepository,
                notificationSubscriberPersistenceMapper
        );
    }

    @Test
    void shouldReturnActiveSubscribersByCategory() {
        // given
        NotificationSubscriber expectedSubscriber = new NotificationSubscriber(
                10L,
                "Alice",
                "alice@example.com",
                "+15550000001",
                Set.of(CategoryCode.SPORTS),
                EnumSet.of(NotificationChannelCode.EMAIL)
        );

        when(categorySubscriptionRepository.findActiveSubscriptionsByCategoryCode("SPORTS"))
                .thenReturn(List.of(sportsSubscription));

        when(sportsSubscription.getUser())
                .thenReturn(user);

        when(user.getId())
                .thenReturn(10L);

        when(categorySubscriptionRepository.findByIdUserId(10L))
                .thenReturn(List.of(sportsSubscription));

        when(channelPreferenceRepository.findByIdUserId(10L))
                .thenReturn(List.of(emailPreference));

        when(notificationSubscriberPersistenceMapper.toDomain(
                user,
                List.of(sportsSubscription),
                List.of(emailPreference)
        )).thenReturn(expectedSubscriber);

        // when
        List<NotificationSubscriber> subscribers =
                cacheService.findActiveSubscribersByCategory(CategoryCode.SPORTS);

        // assert
        assertThat(subscribers).containsExactly(expectedSubscriber);

        NotificationSubscriber subscriber = subscribers.get(0);

        assertThat(subscriber.getId()).isEqualTo(10L);
        assertThat(subscriber.getName()).isEqualTo("Alice");
        assertThat(subscriber.getSubscribedCategories()).containsExactly(CategoryCode.SPORTS);
        assertThat(subscriber.getEnabledChannels()).containsExactly(NotificationChannelCode.EMAIL);
    }
}