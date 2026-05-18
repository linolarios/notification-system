package com.challenge.notification.infrastructure.cache;

import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationChannelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationChannelCacheServiceTest {

    @Mock
    private SpringDataNotificationChannelRepository notificationChannelRepository;

    private NotificationChannelCacheService cacheService;

    @BeforeEach
    void setUp() {
        cacheService = new NotificationChannelCacheService(notificationChannelRepository);
    }

    @Test
    void shouldReturnActiveNotificationChannels() {
        // given
        NotificationChannelEntity email = new NotificationChannelEntity(
                (short) 1,
                "EMAIL",
                "Email",
                true
        );

        when(notificationChannelRepository.findAllByActiveTrueOrderByNameAsc())
                .thenReturn(List.of(email));

        // when
        List<NotificationChannelEntity> channels =
                cacheService.getActiveNotificationChannels();

        // assert
        assertThat(channels).containsExactly(email);
    }

    @Test
    void shouldReturnTrueWhenActiveChannelExists() {
        // given
        NotificationChannelEntity email = new NotificationChannelEntity(
                (short) 1,
                "EMAIL",
                "Email",
                true
        );

        when(notificationChannelRepository.findByCodeAndActiveTrue("EMAIL"))
                .thenReturn(Optional.of(email));

        // when
        boolean exists = cacheService.existsActiveByCode("EMAIL");

        // assert
        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenActiveChannelDoesNotExist() {
        // given
        when(notificationChannelRepository.findByCodeAndActiveTrue("EMAIL"))
                .thenReturn(Optional.empty());

        // when
        boolean exists = cacheService.existsActiveByCode("EMAIL");

        // assert
        assertThat(exists).isFalse();
    }
}
