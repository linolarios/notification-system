package com.challenge.notification.dto.response.mapper;

import com.challenge.notification.dto.response.NotificationLogResponse;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationLogResponseMapperTest {

    private final NotificationLogResponseMapper mapper = new NotificationLogResponseMapper();

    @Test
    void shouldMapNotificationLogEntityToResponse() {
        // given
        CategoryEntity category = new CategoryEntity(
                (short) 1,
                "SPORTS",
                "Sports",
                true
        );

        NotificationChannelEntity channel = new NotificationChannelEntity(
                (short) 1,
                "EMAIL",
                "Email",
                true
        );

        NotificationLogEntity entity = new NotificationLogEntity(
                1L,
                "log-response-correlation",
                100L,
                10L,
                category,
                channel,
                "Alice",
                "alice@example.com",
                "+15550000001",
                "SENT",
                null,
                1,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        // when
        NotificationLogResponse response = mapper.toNotificationLogResponse(entity);

        // assert
        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.correlationId()).isEqualTo("log-response-correlation");
        assertThat(response.messageId()).isEqualTo(100L);
        assertThat(response.userId()).isEqualTo(10L);
        assertThat(response.category()).isEqualTo("SPORTS");
        assertThat(response.channel()).isEqualTo("EMAIL");
        assertThat(response.recipientName()).isEqualTo("Alice");
        assertThat(response.status()).isEqualTo("SENT");
    }
}
