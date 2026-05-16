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

    public NotificationSender getSender(NotificationChannelCode channel) {
        NotificationSender sender = sendersByChannel.get(channel);

        if (sender == null) {
            throw new UnsupportedNotificationChannelException(channel);
        }

        return sender;
    }
}
