package com.challenge.notification.domain.service;

import com.challenge.notification.domain.model.*;
import com.challenge.notification.domain.port.NotificationLogRepositoryPort;
import com.challenge.notification.domain.port.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    private final NotificationSenderRegistry notificationSenderRegistry;
    private final NotificationDeliveryAttemptFactory deliveryAttemptFactory;
    private final NotificationLogRepositoryPort notificationLogRepositoryPort;

    public NotificationDispatcher(
            NotificationSenderRegistry notificationSenderRegistry,
            NotificationDeliveryAttemptFactory deliveryAttemptFactory,
            NotificationLogRepositoryPort notificationLogRepositoryPort
    ) {
        this.notificationSenderRegistry = notificationSenderRegistry;
        this.deliveryAttemptFactory = deliveryAttemptFactory;
        this.notificationLogRepositoryPort = notificationLogRepositoryPort;
    }

    public void dispatch(NotificationMessage message, List<NotificationSubscriber> subscribers) {
        for (NotificationSubscriber subscriber : subscribers) {
            dispatchToSubscriber(message, subscriber);
        }
    }

    private void dispatchToSubscriber(NotificationMessage message, NotificationSubscriber subscriber) {
        for (NotificationChannelCode channel : subscriber.getEnabledChannels()) {
            dispatchToChannel(message, subscriber, channel);
        }
    }

    private void dispatchToChannel(
            NotificationMessage message,
            NotificationSubscriber subscriber,
            NotificationChannelCode channel
    ) {
        NotificationDeliveryAttempt deliveryAttempt;
        try {
            NotificationSender sender = notificationSenderRegistry.getSender(channel);
            NotificationSendResult sendResult = sender.send(message, subscriber);

            deliveryAttempt = sendResult.isSuccess()
                    ? deliveryAttemptFactory.sent(message, subscriber, channel, sendResult)
                    : deliveryAttemptFactory.failed(
                    message,
                    subscriber,
                    channel,
                    sendResult.getErrorMessage().orElse("Unknown notification sending error")
            );
        } catch (Exception exception) {
            log.warn(
                    "Notification delivery failed. messageId : {} subscriberId : {} channel : {}",
                    message.getId(),
                    subscriber.getId(),
                    channel,
                    exception
            );

            deliveryAttempt = deliveryAttemptFactory.failed(
                    message,
                    subscriber,
                    channel,
                    exception.getMessage()
            );
        }
        notificationLogRepositoryPort.save(deliveryAttempt);
    }
}
