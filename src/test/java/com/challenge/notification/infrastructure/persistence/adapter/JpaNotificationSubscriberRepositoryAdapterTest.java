package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.infrastructure.cache.NotificationSubscriberCacheService;
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
class JpaNotificationSubscriberRepositoryAdapterTest {

    @Mock
    private NotificationSubscriberCacheService notificationSubscriberCacheService;

    private JpaNotificationSubscriberRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaNotificationSubscriberRepositoryAdapter(
                notificationSubscriberCacheService
        );
    }

    @Test
    void shouldFindActiveSubscribersByCategory() {
        // given
        NotificationSubscriber subscriber = new NotificationSubscriber(
                10L,
                "Alice",
                "alice@example.com",
                "+15550000001",
                Set.of(CategoryCode.SPORTS),
                EnumSet.of(NotificationChannelCode.EMAIL)
        );

        when(notificationSubscriberCacheService.findActiveSubscribersByCategory(CategoryCode.SPORTS))
                .thenReturn(List.of(subscriber));

        // when
        List<NotificationSubscriber> subscribers =
                adapter.findActiveSubscribersByCategory(CategoryCode.SPORTS);

        // assert
        assertThat(subscribers).containsExactly(subscriber);
    }
}
