package com.challenge.notification.domain.service.service2;

import com.challenge.notification.domain.exception.UnsupportedNotificationChannelException;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSendResult;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.domain.port.NotificationSender;
import com.challenge.notification.domain.service.NotificationSenderRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationSenderRegistryTest {

    @Test
    void shouldReturnSenderForSupportedChannel() {
        NotificationSender emailSender = new FakeNotificationSender(NotificationChannelCode.EMAIL);

        NotificationSenderRegistry registry = new NotificationSenderRegistry(List.of(emailSender));

        NotificationSender resolvedSender = registry.getSender(NotificationChannelCode.EMAIL);

        assertThat(resolvedSender).isSameAs(emailSender);
    }

    @Test
    void shouldThrowExceptionWhenChannelIsUnsupported() {
        NotificationSender emailSender = new FakeNotificationSender(NotificationChannelCode.EMAIL);

        NotificationSenderRegistry registry = new NotificationSenderRegistry(List.of(emailSender));

        assertThatThrownBy(() -> registry.getSender(NotificationChannelCode.SMS))
                .isInstanceOf(UnsupportedNotificationChannelException.class)
                .hasMessageContaining("Unsupported notification channel");
    }

    @Test
    void shouldThrowExceptionWhenDuplicateSenderExistsForSameChannel() {
        NotificationSender firstEmailSender = new FakeNotificationSender(NotificationChannelCode.EMAIL);
        NotificationSender secondEmailSender = new FakeNotificationSender(NotificationChannelCode.EMAIL);

        assertThatThrownBy(() -> new NotificationSenderRegistry(List.of(firstEmailSender, secondEmailSender)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Duplicate notification sender");
    }

    private static class FakeNotificationSender implements NotificationSender {

        private final NotificationChannelCode supportedChannel;

        private FakeNotificationSender(NotificationChannelCode supportedChannel) {
            this.supportedChannel = supportedChannel;
        }

        @Override
        public NotificationChannelCode getSupportedChannel() {
            return supportedChannel;
        }

        @Override
        public NotificationSendResult send(NotificationMessage message, NotificationSubscriber subscriber) {
            return NotificationSendResult.success("fake-provider-id");
        }
    }
}
