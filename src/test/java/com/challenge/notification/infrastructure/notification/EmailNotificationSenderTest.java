package com.challenge.notification.infrastructure.notification;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSendResult;
import com.challenge.notification.domain.model.NotificationSubscriber;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class EmailNotificationSenderTest {

    private final EmailNotificationSender sender = new EmailNotificationSender();

    @Test
    void shouldSupportEmailChannel() {
        // given / when
        NotificationChannelCode supportedChannel = sender.getSupportedChannel();

        // assert
        assertThat(supportedChannel).isEqualTo(NotificationChannelCode.EMAIL);
    }

    @Test
    void shouldReturnSuccessWhenSendingEmail() {
        // given
        NotificationMessage message = message();
        NotificationSubscriber subscriber = subscriber();

        // when
        NotificationSendResult result = sender.send(message, subscriber);

        // assert
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getProviderMessageId().toLowerCase()).contains("e-mail");
        assertThat(result.getSentAt()).isNotNull();
    }

    private static NotificationMessage message() {
        return new NotificationMessage(
                1L,
                "email-test-correlation",
                CategoryCode.SPORTS,
                "Email message",
                LocalDateTime.now()
        );
    }

    private static NotificationSubscriber subscriber() {
        return new NotificationSubscriber(
                10L,
                "Alice",
                "alice@example.com",
                "+15550000001",
                Set.of(CategoryCode.SPORTS),
                EnumSet.of(NotificationChannelCode.EMAIL)
        );
    }
}
