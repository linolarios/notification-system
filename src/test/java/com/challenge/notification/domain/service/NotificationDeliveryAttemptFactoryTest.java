package com.challenge.notification.domain.service;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationDeliveryAttempt;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSendResult;
import com.challenge.notification.domain.model.NotificationStatus;
import com.challenge.notification.domain.model.NotificationSubscriber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationDeliveryAttemptFactoryTest {

    private NotificationDeliveryAttemptFactory factory;

    @BeforeEach
    void setUp() {
        factory = new NotificationDeliveryAttemptFactory();
    }

    @Test
    void shouldCreateSentDeliveryAttempt() {
        // given
        NotificationMessage message = message();
        NotificationSubscriber subscriber = subscriber();
        NotificationSendResult sendResult = NotificationSendResult.success("provider-message-123");

        // when
        NotificationDeliveryAttempt attempt = factory.sent(
                message,
                subscriber,
                NotificationChannelCode.EMAIL,
                sendResult
        );

        // assert
        assertThat(attempt.getCorrelationId()).isEqualTo("factory-test-correlation");
        assertThat(attempt.getMessageId()).isEqualTo(100L);
        assertThat(attempt.getUserId()).isEqualTo(10L);
        assertThat(attempt.getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(attempt.getChannel()).isEqualTo(NotificationChannelCode.EMAIL);
        assertThat(attempt.getRecipientName()).isEqualTo("Alice");
        assertThat(attempt.getRecipientEmail()).isEqualTo("alice@example.com");
        assertThat(attempt.getRecipientPhoneNumber()).isEqualTo("+15550000001");
        assertThat(attempt.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(attempt.getErrorMessage()).isNull();
        assertThat(attempt.getAttemptCount()).isEqualTo(NotificationDeliveryAttempt.FIRST_ATTEMPT_COUNT);
        assertThat(attempt.getSentAt()).isNotNull();
        assertThat(attempt.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldCreateFailedDeliveryAttempt() {
        // given
        NotificationMessage message = message();
        NotificationSubscriber subscriber = subscriber();

        // when
        NotificationDeliveryAttempt attempt = factory.failed(
                message,
                subscriber,
                NotificationChannelCode.SMS,
                "Provider unavailable"
        );

        // assert
        assertThat(attempt.getCorrelationId()).isEqualTo("factory-test-correlation");
        assertThat(attempt.getMessageId()).isEqualTo(100L);
        assertThat(attempt.getUserId()).isEqualTo(10L);
        assertThat(attempt.getCategory()).isEqualTo(CategoryCode.SPORTS);
        assertThat(attempt.getChannel()).isEqualTo(NotificationChannelCode.SMS);
        assertThat(attempt.getRecipientName()).isEqualTo("Alice");
        assertThat(attempt.getRecipientEmail()).isEqualTo("alice@example.com");
        assertThat(attempt.getRecipientPhoneNumber()).isEqualTo("+15550000001");
        assertThat(attempt.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(attempt.getErrorMessage()).isEqualTo("Provider unavailable");
        assertThat(attempt.getAttemptCount()).isEqualTo(NotificationDeliveryAttempt.FIRST_ATTEMPT_COUNT);
        assertThat(attempt.getSentAt()).isNull();
        assertThat(attempt.getCreatedAt()).isNotNull();
    }

    private static NotificationMessage message() {
        return new NotificationMessage(
                100L,
                "factory-test-correlation",
                CategoryCode.SPORTS,
                "Game starts tonight",
                LocalDateTime.now()
        );
    }

    private static NotificationSubscriber subscriber() {
        return new NotificationSubscriber(
                10L,
                "Alice",
                "alice@example.com",
                "+15550000001",
                EnumSet.of(CategoryCode.SPORTS),
                EnumSet.of(NotificationChannelCode.EMAIL, NotificationChannelCode.SMS)
        );
    }
}
