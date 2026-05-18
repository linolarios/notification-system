package com.challenge.notification.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationDeliveryAttemptTest {

    @Test
    void shouldCreateSentDeliveryAttempt() {
        // given
        NotificationMessage message = message();
        NotificationSubscriber subscriber = subscriber(NotificationChannelCode.EMAIL);
        NotificationSendResult sendResult = NotificationSendResult.success("provider-id");

        // when
        NotificationDeliveryAttempt attempt = NotificationDeliveryAttempt.sent(
                message,
                subscriber,
                NotificationChannelCode.EMAIL,
                sendResult
        );

        // assert
        assertThat(attempt.getCorrelationId()).isEqualTo(message.getCorrelationId());
        assertThat(attempt.getMessageId()).isEqualTo(message.getId());
        assertThat(attempt.getUserId()).isEqualTo(subscriber.getId());
        assertThat(attempt.getChannel()).isEqualTo(NotificationChannelCode.EMAIL);
        assertThat(attempt.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(attempt.getErrorMessage()).isNull();
        assertThat(attempt.getSentAt()).isNotNull();
        assertThat(attempt.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void shouldCreateFailedDeliveryAttempt() {
        // given
        NotificationMessage message = message();
        NotificationSubscriber subscriber = subscriber(NotificationChannelCode.SMS);

        // when
        NotificationDeliveryAttempt attempt = NotificationDeliveryAttempt.failed(
                message,
                subscriber,
                NotificationChannelCode.SMS,
                "Provider unavailable"
        );

        // assert
        assertThat(attempt.getStatus()).isEqualTo(NotificationStatus.FAILED);
        assertThat(attempt.getChannel()).isEqualTo(NotificationChannelCode.SMS);
        assertThat(attempt.getCorrelationId()).isEqualTo(message.getCorrelationId());
        assertThat(attempt.getMessageId()).isEqualTo(message.getId());
        assertThat(attempt.getUserId()).isEqualTo(subscriber.getId());
        assertThat(attempt.getErrorMessage()).isEqualTo("Provider unavailable");
        assertThat(attempt.getSentAt()).isNull();
        assertThat(attempt.getAttemptCount()).isEqualTo(1);
    }

    @Test
    void shouldIncrementAttemptCountImmutably() {
        // given
        NotificationMessage message = message();
        NotificationSubscriber subscriber = subscriber(NotificationChannelCode.EMAIL);

        NotificationDeliveryAttempt attempt = NotificationDeliveryAttempt.sent(
                message,
                subscriber,
                NotificationChannelCode.EMAIL,
                NotificationSendResult.success("provider-id")
        );

        // when
        NotificationDeliveryAttempt incrementedAttempt = attempt.withIncrementedAttemptCount();

        // assert
        assertThat(attempt.getAttemptCount()).isEqualTo(1);
        assertThat(incrementedAttempt.getAttemptCount()).isEqualTo(2);
        assertThat(incrementedAttempt).isNotSameAs(attempt);
    }

    @Test
    void shouldRequireEmailForEmailDelivery() {
        // given
        NotificationDeliveryAttempt.Builder builder = baseBuilder()
                .channel(NotificationChannelCode.EMAIL)
                .recipientEmail(null)
                .recipientPhoneNumber("+15550000001")
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now());

        // when / assert
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("EMAIL delivery attempt requires recipientEmail");
    }

    @Test
    void shouldRequirePhoneNumberForSmsDelivery() {
        // given
        NotificationDeliveryAttempt.Builder builder = baseBuilder()
                .channel(NotificationChannelCode.SMS)
                .recipientEmail("alice@example.com")
                .recipientPhoneNumber(null)
                .status(NotificationStatus.SENT)
                .sentAt(LocalDateTime.now());

        // when / assert
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SMS delivery attempt requires recipientPhoneNumber");
    }

    @Test
    void shouldRequireSentAtForSentAttempt() {
        // given
        NotificationDeliveryAttempt.Builder builder = baseBuilder()
                .channel(NotificationChannelCode.EMAIL)
                .recipientEmail("alice@example.com")
                .recipientPhoneNumber("+15550000001")
                .status(NotificationStatus.SENT)
                .sentAt(null);

        // when / assert
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SENT delivery attempt must have sentAt");
    }

    @Test
    void shouldRequireErrorMessageForFailedAttempt() {
        // given
        NotificationDeliveryAttempt.Builder builder = baseBuilder()
                .channel(NotificationChannelCode.EMAIL)
                .recipientEmail("alice@example.com")
                .recipientPhoneNumber("+15550000001")
                .status(NotificationStatus.FAILED)
                .errorMessage(null);

        // when / assert
        assertThatThrownBy(builder::build)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("FAILED delivery attempt must have errorMessage");
    }

    private static NotificationDeliveryAttempt.Builder baseBuilder() {
        return NotificationDeliveryAttempt.builder()
                .correlationId("attempt-test-correlation")
                .messageId(1L)
                .userId(10L)
                .category(CategoryCode.SPORTS)
                .recipientName("Alice")
                .attemptCount(1)
                .createdAt(LocalDateTime.now());
    }

    private static NotificationMessage message() {
        return new NotificationMessage(
                1L,
                "attempt-test-correlation",
                CategoryCode.SPORTS,
                "Test message",
                LocalDateTime.now()
        );
    }

    private static NotificationSubscriber subscriber(NotificationChannelCode channel) {
        return new NotificationSubscriber(
                10L,
                "Alice",
                "alice@example.com",
                "+15550000001",
                Set.of(CategoryCode.SPORTS),
                EnumSet.of(channel)
        );
    }
}
