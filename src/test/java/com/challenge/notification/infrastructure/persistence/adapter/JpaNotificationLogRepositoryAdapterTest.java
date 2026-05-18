package com.challenge.notification.infrastructure.persistence.adapter;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationDeliveryAttempt;
import com.challenge.notification.domain.model.NotificationStatus;
import com.challenge.notification.infrastructure.persistence.entity.CategoryEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationChannelEntity;
import com.challenge.notification.infrastructure.persistence.entity.NotificationLogEntity;
import com.challenge.notification.infrastructure.persistence.mapper.NotificationLogPersistenceMapper;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataCategoryRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationChannelRepository;
import com.challenge.notification.infrastructure.persistence.repository.SpringDataNotificationLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaNotificationLogRepositoryAdapterTest {

    @Mock
    private SpringDataNotificationLogRepository notificationLogRepository;

    @Mock
    private SpringDataCategoryRepository categoryRepository;

    @Mock
    private SpringDataNotificationChannelRepository channelRepository;

    private JpaNotificationLogRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new JpaNotificationLogRepositoryAdapter(
                notificationLogRepository,
                categoryRepository,
                channelRepository,
                new NotificationLogPersistenceMapper()
        );
    }

    @Test
    void shouldSaveNotificationDeliveryAttempt() {
        // given
        CategoryEntity category = categoryEntity();
        NotificationChannelEntity channel = channelEntity();

        NotificationDeliveryAttempt attempt = deliveryAttempt();

        when(categoryRepository.findByCodeAndActiveTrue("SPORTS"))
                .thenReturn(Optional.of(category));

        when(channelRepository.findByCodeAndActiveTrue("EMAIL"))
                .thenReturn(Optional.of(channel));

        when(notificationLogRepository.save(any(NotificationLogEntity.class)))
                .thenAnswer(invocation -> {
                    NotificationLogEntity entity = invocation.getArgument(0);

                    return new NotificationLogEntity(
                            1L,
                            entity.getCorrelationId(),
                            entity.getMessageId(),
                            entity.getUserId(),
                            entity.getCategory(),
                            entity.getChannel(),
                            entity.getRecipientName(),
                            entity.getRecipientEmail(),
                            entity.getRecipientPhoneNumber(),
                            entity.getStatus(),
                            entity.getErrorMessage(),
                            entity.getAttemptCount(),
                            entity.getSentAt(),
                            entity.getCreatedAt()
                    );
                });

        // when
        NotificationDeliveryAttempt savedAttempt = adapter.save(attempt);

        // assert
        assertThat(savedAttempt.getId()).isEqualTo(1L);
        assertThat(savedAttempt.getCorrelationId()).isEqualTo("log-adapter-correlation");
        assertThat(savedAttempt.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(savedAttempt.getChannel()).isEqualTo(NotificationChannelCode.EMAIL);
    }

    private static NotificationDeliveryAttempt deliveryAttempt() {
        return NotificationDeliveryAttempt.builder()
                .correlationId("log-adapter-correlation")
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
