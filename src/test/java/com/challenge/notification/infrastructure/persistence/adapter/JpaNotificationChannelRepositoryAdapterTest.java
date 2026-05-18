package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.infrastructure.cache.NotificationChannelCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaNotificationChannelRepositoryAdapterTest {

    @Mock
    private NotificationChannelCacheService notificationChannelCacheService;

    private JpaNotificationChannelRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaNotificationChannelRepositoryAdapter(notificationChannelCacheService);
    }

    @Test
    void shouldReturnTrueWhenActiveChannelExists() {
        // given
        when(notificationChannelCacheService.existsActiveByCode("EMAIL"))
                .thenReturn(true);

        // when
        boolean exists = adapter.existsActiveByCode(NotificationChannelCode.EMAIL);

        // assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenActiveChannelDoesNotExist() {
        // given
        when(notificationChannelCacheService.existsActiveByCode("EMAIL"))
                .thenReturn(false);

        // when
        boolean exists = adapter.existsActiveByCode(NotificationChannelCode.EMAIL);

        // assert
        assertThat(exists).isFalse();
    }
}
