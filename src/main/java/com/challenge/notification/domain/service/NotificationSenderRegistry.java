package com.challenge.notification.domain.service;

import com.challenge.notification.domain.exception.UnsupportedNotificationChannelException;
import com.challenge.notification.domain.model.NotificationChannelCode;
import com.challenge.notification.domain.port.NotificationSender;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationSenderRegistry {

    private final Map<NotificationChannelCode, NotificationSender> sendersByChannel;

    /**
     * Builds a registry of notification senders keyed by their supported channel.
     *
     * <p>Exactly one sender is allowed per channel. Duplicate senders are treated as an
     * application configuration error and fail fast during startup.</p>
     */
    public NotificationSenderRegistry(List<NotificationSender> notificationSenders) {
        this.sendersByChannel = new EnumMap<>(NotificationChannelCode.class);

        for (NotificationSender sender : notificationSenders) {
            NotificationSender previousSender = sendersByChannel.put(
                    sender.getSupportedChannel(),
                    sender
            );

            if (previousSender != null) {
                throw new IllegalStateException(
                        "Duplicate notification sender for channel: " + sender.getSupportedChannel()
                );
            }
        }
    }

    /**
     * Returns the sender responsible for the requested notification channel.
     *
     * @param channel notification channel to dispatch through
     * @return sender implementation for the channel
     * @throws UnsupportedNotificationChannelException when no sender is registered for the channel
     */
    public NotificationSender getSender(NotificationChannelCode channel) {
        NotificationSender sender = sendersByChannel.get(channel);

        if (sender == null) {
            throw new UnsupportedNotificationChannelException(channel);
        }

        return sender;
    }
}
