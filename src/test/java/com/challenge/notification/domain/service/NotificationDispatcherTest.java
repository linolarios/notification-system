package com.challenge.notification.domain.service;

import com.challenge.notification.domain.model.CategoryCode;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.model.NotificationDeliveryAttempt;
import com.challenge.notification.domain.model.NotificationMessage;
import com.challenge.notification.domain.model.NotificationSendResult;
import com.challenge.notification.domain.model.NotificationStatus;
import com.challenge.notification.domain.model.NotificationSubscriber;
import com.challenge.notification.domain.port.NotificationLogRepositoryPort;
import com.challenge.notification.domain.port.NotificationSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationDispatcherTest {

    @Mock
    private NotificationSenderRegistry notificationSenderRegistry;

    @Mock
    private NotificationLogRepositoryPort notificationLogRepositoryPort;

    @Mock
    private NotificationSender emailSender;

    @Mock
    private NotificationSender smsSender;

    private NotificationDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        dispatcher = new NotificationDispatcher(
                notificationSenderRegistry,
                new NotificationDeliveryAttemptFactory(),
                notificationLogRepositoryPort
        );
    }

    @Test
    void shouldCreateSentLogWhenSenderSucceeds() {
        NotificationMessage message = message();
        NotificationSubscriber subscriber = subscriberWithChannels(NotificationChannelCode.EMAIL);

        when(notificationSenderRegistry.getSender(NotificationChannelCode.EMAIL))
                .thenReturn(emailSender);

        when(emailSender.send(message, subscriber))
                .thenReturn(NotificationSendResult.success("provider-id"));

        when(notificationLogRepositoryPort.save(any(NotificationDeliveryAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dispatcher.dispatch(message, List.of(subscriber));

        ArgumentCaptor<NotificationDeliveryAttempt> captor =
                ArgumentCaptor.forClass(NotificationDeliveryAttempt.class);

        verify(notificationLogRepositoryPort).save(captor.capture());

        NotificationDeliveryAttempt savedAttempt = captor.getValue();

        assertThat(savedAttempt.getStatus()).isEqualTo(NotificationStatus.SENT);
        assertThat(savedAttempt.getChannel()).isEqualTo(NotificationChannelCode.EMAIL);
        assertThat(savedAttempt.getCorrelationId()).isEqualTo(message.getCorrelationId());
        assertThat(savedAttempt.getMessageId()).isEqualTo(message.getId());
        assertThat(savedAttempt.getUserId()).isEqualTo(subscriber.getId());
    }

    @Test
    void shouldContinueProcessingWhenOneSenderFails() {
        NotificationMessage message = message();
        NotificationSubscriber subscriber = subscriberWithChannels(
                NotificationChannelCode.EMAIL,
                NotificationChannelCode.SMS
        );

        when(notificationSenderRegistry.getSender(NotificationChannelCode.EMAIL))
                .thenReturn(emailSender);

        when(notificationSenderRegistry.getSender(NotificationChannelCode.SMS))
                .thenReturn(smsSender);

        when(emailSender.send(message, subscriber))
                .thenThrow(new RuntimeException("Email provider unavailable"));

        when(smsSender.send(message, subscriber))
                .thenReturn(NotificationSendResult.success("sms-provider-id"));

        when(notificationLogRepositoryPort.save(any(NotificationDeliveryAttempt.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        dispatcher.dispatch(message, List.of(subscriber));

        ArgumentCaptor<NotificationDeliveryAttempt> captor =
                ArgumentCaptor.forClass(NotificationDeliveryAttempt.class);

        verify(notificationLogRepositoryPort, org.mockito.Mockito.times(2))
                .save(captor.capture());

        assertThat(captor.getAllValues())
                .extracting(NotificationDeliveryAttempt::getStatus)
                .containsExactlyInAnyOrder(NotificationStatus.FAILED, NotificationStatus.SENT);
    }

    private static NotificationMessage message() {
        return new NotificationMessage(
                1L,
                "dispatcher-test-correlation",
                CategoryCode.SPORTS,
                "Test message",
                LocalDateTime.now()
        );
    }

    private static NotificationSubscriber subscriberWithChannels(NotificationChannelCode... channels) {
        return new NotificationSubscriber(
                10L,
                "Alice",
                "alice@example.com",
                "+15550000001",
                Set.of(CategoryCode.SPORTS),
                EnumSet.copyOf(List.of(channels))
        );
    }
}
