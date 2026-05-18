package com.challenge.notification.infrastructure.persistence.mapper;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationDeliveryAttempt;
import com.challenge.notification.domain.model.NotificationStatus;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationLogPersistenceMapperTest {

    private final NotificationLogPersistenceMapper mapper = new NotificationLogPersistenceMapper();

    @Test
    void shouldMapDomainToEntity() {
        // given
        CategoryEntity categoryEntity = categoryEntity();
        NotificationChannelEntity channelEntity = channelEntity();

        NotificationDeliveryAttempt attempt = NotificationDeliveryAttempt.builder()
                .id(1L)
                .correlationId("log-mapper-correlation")
                .messageId(100L)
                .userId(10L)
                .category(CategoryCode.SPORTS)
                .channel(NotificationChannelCode.EMAIL)
                .recipientName("Alice")
                .recipientEmail("alice@example.com")
                .recipientPhoneNumber("+15550000001")
                .status(NotificationStatus.SENT)
                .attemptCount(1)
                .sentAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        // when
        NotificationLogEntity entity = mapper.toEntity(
                attempt,
                categoryEntity,
                channelEntity
        );

        // assert
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getCorrelationId()).isEqualTo("log-mapper-correlation");
        assertThat(entity.getCategory()).isSameAs(categoryEntity);
        assertThat(entity.getChannel()).isSameAs(channelEntity);
        assertThat(entity.getStatus()).isEqualTo("SENT");
    }

    @Test
    void shouldMapEntityToDomain() {
        // given
        NotificationLogEntity entity = new NotificationLogEntity(
                1L,
                "log-mapper-correlation",
                100L,
                10L,
                categoryEntity(),
                channelEntity(),
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
        NotificationDeliveryAttempt attempt = mapper.toDomain(entity);

        // assert
        assertThat(attempt.getId()).isEqualTo(1L);
        assertThat(attempt.getCorrelationId()).isEqualTo("log-mapper-correlation");
        assertThat(attempt.getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(attempt.getChannel()).isEqualTo(NotificationChannelCode.EMAIL);
        assertThat(attempt.getStatus()).isEqualTo(NotificationStatus.SENT);
    }

    private static CategoryEntity categoryEntity() {
        return new CategoryEntity(
                (short) 1,
                "SPORTS",
                "Sports",
                true
        );
    }

    private static NotificationChannelEntity channelEntity() {
        return new NotificationChannelEntity(
                (short) 1,
                "EMAIL",
                "Email",
                true
        );
    }
}
